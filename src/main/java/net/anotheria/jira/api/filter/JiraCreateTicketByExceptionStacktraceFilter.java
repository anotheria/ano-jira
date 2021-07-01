package net.anotheria.jira.api.filter;

import com.atlassian.jira.rest.client.api.domain.Issue;
import net.anotheria.jira.api.bean.JiraExceptionTicketPO;
import net.anotheria.jira.api.config.JiraTicketConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class JiraCreateTicketByExceptionStacktraceFilter implements JiraCreateTicketByExceptionFilter {

    /**
     * Logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(JiraCreateTicketByExceptionStacktraceFilter.class);

    /**
     * Compare 5 stacktrace lines by default
     */
    private static final int LINES_TO_COMPARE = 5;

    /**
     * Search all non closed ticket in given epic
     */
    private static final String SEARCH_QUERY = "project = %s AND \"Epic Link\" = %s and status != \"Closed\"";

    /**
     * Ignore all stacktrace lines which started with this
     */
    public static final String[] EXCLUSION_LINES = {
            "at sun.reflect",
            "at java.lang.reflect",
            "at com.sun.proxy",
            "at com.sun.jersey",
            "at org.aspectj.runtime",
            "at net.anotheria.moskito",
            "at net.anotheria.anoplass"
    };

    /**
     * Get search query
     */
    @Override
    public String getSearchQuery(String epicKey) {
        JiraTicketConfig config = JiraTicketConfig.getInstance();
        return String.format(SEARCH_QUERY, config.getProjectKey(), epicKey);
    }

    /**
     * Get ignored lines. Override if necessary
     */
    public String[] getStacktraceExclusionLines() {
        return EXCLUSION_LINES;
    }

    /**
     * Get line number to compare. Override if necessary
     */
    public int getLinesToCompare() {
        return LINES_TO_COMPARE;
    }

    /**
     * Check ticket if already exists
     */
    @Override
    public boolean isSame(JiraExceptionTicketPO ticket, Issue issue) {
        String description = issue.getDescription();
        if (description == null)
            return false;

        String[] descriptionSplit = description.replaceAll("[\r\t]", "").split("\n");
        List<String> descriptionSplitArr = skipTrashLines(descriptionSplit);

        // Find stacktrace block
        int start = -1;
        for (int i = 0; i < descriptionSplitArr.size(); i++) {
            if (descriptionSplitArr.get(i).contains(STACKTRACE_START)) {
                start = i+1;
                break;
            }
        }

        if (start == -1) {
            return false;
        }

        // Copy 1..LINES_TO_COMPARE lines from stacktrace in description
        List<String> descriptionLines = descriptionSplitArr.subList(start, Math.min(descriptionSplitArr.size(), start + getLinesToCompare()));

        // Copy 1..LINES_TO_COMPARE lines from stacktrace in ticket
        String[] stackTraceSplit = ticket.getStackTrace().replaceAll("[\r\t]", "").split("\n");
        List<String> stackTraceSplitArr = skipTrashLines(stackTraceSplit);
        List<String> stackTraceLines = stackTraceSplitArr.subList(1, Math.min(stackTraceSplitArr.size(), 1 + getLinesToCompare())); // start - 1st line

        // Compare lines
        for (int i = 0; i < Math.min(descriptionLines.size(), stackTraceLines.size()); i++) {
            if (!descriptionLines.get(i).equals(stackTraceLines.get(i))) {
                return false;
            }
        }

        return true;
    }

    /**
     * Skip lines from stacktrace
     */
    private List<String> skipTrashLines(String[] lines) {
        List<String> result = Arrays.stream(lines).filter(line -> {
            for (String exclude : getStacktraceExclusionLines()) {
                if (line.startsWith(exclude))
                    return false;
            }
            return true;
        }).collect(Collectors.toList());

        return result;
    }
}
