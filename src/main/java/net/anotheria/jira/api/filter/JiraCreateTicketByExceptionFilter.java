package net.anotheria.jira.api.filter;

import com.atlassian.jira.rest.client.api.domain.Issue;
import net.anotheria.jira.api.bean.JiraExceptionTicketPO;

/**
 * Filter to check if same/similar ticket is already created.
 */
public interface JiraCreateTicketByExceptionFilter {

    String STACKTRACE_START = "{panel:title=Stacktrace}";

    /**
     * Jira search query to retrieve all potential similar ticket
     */
    String getSearchQuery(String epicKey);

    /**
     * Check if jira ticker is same as new ticket
     */
    boolean isSame(JiraExceptionTicketPO ticket, Issue issue);
}
