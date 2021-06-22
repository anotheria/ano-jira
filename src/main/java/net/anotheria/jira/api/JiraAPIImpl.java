package net.anotheria.jira.api;

import com.atlassian.jira.rest.client.api.AuthenticationHandler;
import com.atlassian.jira.rest.client.api.IssueRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.ProjectRestClient;
import com.atlassian.jira.rest.client.api.SearchRestClient;
import com.atlassian.jira.rest.client.api.domain.BasicIssue;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.IssueField;
import com.atlassian.jira.rest.client.api.domain.IssueFieldId;
import com.atlassian.jira.rest.client.api.domain.Project;
import com.atlassian.jira.rest.client.api.domain.SearchResult;
import com.atlassian.jira.rest.client.api.domain.input.IssueInput;
import com.atlassian.jira.rest.client.api.domain.input.IssueInputBuilder;
import com.atlassian.jira.rest.client.auth.BasicHttpAuthenticationHandler;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import io.atlassian.util.concurrent.Promise;
import net.anotheria.anoplass.api.APIInitException;
import net.anotheria.anoplass.api.AbstractAPIImpl;
import net.anotheria.jira.api.config.JiraTicketConfig;
import net.anotheria.jira.api.bean.JiraExceptionTicketPO;
import net.anotheria.jira.api.filter.JiraCreateTicketByExceptionFilter;

import java.net.URI;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Jira API basic Impl
 */
public class JiraAPIImpl extends AbstractAPIImpl implements JiraAPI {

	/**
	 * Retrieve tickets number in each call
	 */
	private static final int MAX_PER_QUERY = 20;

    /**
     * Search epic ticket by title
     */
    private static final String EPIC_SEARCH_QUERY = "project = %s AND \"Epic Name\" = \"%s\" ORDER BY key DESC";

	/**
	 * Jira ticket check filter
	 */
	private JiraCreateTicketByExceptionFilter taskFilter;

	/**
	 * Constructor
	 */
	public JiraAPIImpl(JiraCreateTicketByExceptionFilter taskFilter) {
		this.taskFilter = taskFilter;
	}

	/**
	 * Init
	 */
	@Override
	public void init() throws APIInitException {
		super.init();

	}

	/**
	 * Check if such ticket is already created. Create new if not.
	 */
	@Override
	public void createJiraTicket(JiraExceptionTicketPO ticket) {

        JiraRestClient restClient = null;
        try {
            JiraTicketConfig config = JiraTicketConfig.getInstance();

            if (!config.isEnabled()) {
            	return;
			}

			URI jiraServerUri = URI.create(config.getBaseJiraUrl());
			AsynchronousJiraRestClientFactory factory = new AsynchronousJiraRestClientFactory();
			AuthenticationHandler auth = new BasicHttpAuthenticationHandler(config.getJiraMasterAcc(), config.getJiraMasterPass());
			restClient = factory.create(jiraServerUri, auth);
			ProjectRestClient projectClient = restClient.getProjectClient();
			IssueRestClient issueClient = restClient.getIssueClient();
			SearchRestClient searchClient = restClient.getSearchClient();

			if (exists(searchClient, ticket))
				return;

            Issue epicIssue = getEpicIssue(issueClient, searchClient);
            Iterator<IssueField> attachments  = epicIssue.getFields().iterator();
            String epicLinkFieldId = null;
            while (attachments.hasNext()){
                IssueField field = attachments.next();
                if (field.getName().equals("Epic Link")) {
                    epicLinkFieldId = field.getId();
                }
            }

            Long priority = ticket.getPriority() != null ? ticket.getPriority() : config.getDefaultPriority();
            String reporterName = ticket.getReporterName() != null ? ticket.getReporterName() : config.getDefaultReporterName();
            String assigneeName = ticket.getAssigneeName() != null ? ticket.getAssigneeName() : config.getDefaultAssigneeName();
            List<String> labels = ticket.getLabels() != null ? ticket.getLabels() : Arrays.asList(config.getLabels());
            String title = ticket.getTitle() != null ? ticket.getTitle() : (ticket.getTitlePrefix() != null ? ticket.getTitlePrefix() + ticket.getExceptionName() : config.getDefaultSummaryPrefix() + ticket.getExceptionName());

			Project project = projectClient.getProject(config.getProjectKey()).claim();

			IssueInputBuilder iib = new IssueInputBuilder(project.getKey(), 1L); // 1L - have to be Bug
			iib.setSummary(title);
			iib.setPriorityId(priority);
			iib.setDescription(getDescription(ticket));
			iib.setReporterName(reporterName);
			iib.setAssigneeName(assigneeName);
			iib.setFieldValue(IssueFieldId.LABELS_FIELD.id, labels);
			if (epicLinkFieldId != null) {
                iib.setFieldValue(epicLinkFieldId, config.getEpicKey());
            }

			// samples
//            iib.setFieldInput(new FieldInput("cust_field_5445", "Custom Value 1"));
//            iib.setFieldInput(new FieldInput("cust_field_599", ComplexIssueInputFieldValue.with("value", "Testing")));
//            iib.setFieldInput(new FieldInput(IssueFieldId.COMPONENTS_FIELD, "value"));

			IssueInput issue = iib.build();
			BasicIssue issueObj = issueClient.createIssue(issue).claim();

//			System.out.println("Key : " + issueObj.getKey());
        } catch (Exception e) {
            log.error("Can't create jira task", e);
        } finally {
			try {
			    if (restClient != null) {
                    restClient.close();
                }
			} catch (Exception e) {
				log.error("Can't close rest client", e);
			}
		}
	}

    /**
     * Get epic issue by name
     */
    private Issue getEpicIssue(IssueRestClient issueClient, SearchRestClient searchClient) {
        JiraTicketConfig config = JiraTicketConfig.getInstance();
        String searchQuery = String.format(EPIC_SEARCH_QUERY, config.getProjectKey(), config.getEpicName());

        Promise<SearchResult> searchResult = searchClient.searchJql(searchQuery, 1, 0, null);
        SearchResult results = searchResult.claim();
        if (results.getTotal() > 0)
            return results.getIssues().iterator().next();

        Issue issue = issueClient.getIssue(config.getEpicKey()).claim();
        return  issue;
    }

    /**
	 * Get ticket body
	 */
	private String getDescription(JiraExceptionTicketPO ticket) {
		StringBuilder sb = new StringBuilder();
		sb.append("Automatically generated ticket").append("\r\n\r\n")
		.append("{panel:title=Error ID}").append(ticket.getUuid().toString()).append("{panel}").append("\r\n")
		.append("{panel:title=Error Message}{noformat}").append(ticket.getExceptionName()).append(" ").append(ticket.getExceptionMessage()).append("{noformat}{panel}").append("\r\n\r\n")

		.append("{panel:title=Reported user id}").append(ticket.getAccountId()).append("{panel}").append("\r\n")
		.append("{panel:title=Reported user mail}").append(ticket.getAccountEmail()).append("{panel}").append("\r\n\r\n")

		.append("{panel:title=Page URL}").append(ticket.getPageUrl()).append("{panel}").append("\r\n")
		.append("{panel:title=Error Endpoint}").append(ticket.getErrorEndpoint()).append("{panel}").append("\r\n")
		.append("{panel:title=Reported}").append(ticket.getDateStr()).append("{panel}").append("\r\n\r\n")

		.append(JiraCreateTicketByExceptionFilter.STACKTRACE_START + "{noformat}").append(ticket.getStackTrace()).append("{noformat}{panel}").append("\r\n");

		return sb.toString();
	}

	/**
	 * Check if same ticket
	 */
	private boolean exists(SearchRestClient searchClient, JiraExceptionTicketPO ticket) {
		String jql = taskFilter.getSearchQuery();

		int startIndex = 0;
		while (true) {
			Promise<SearchResult> searchResult = searchClient.searchJql(jql, MAX_PER_QUERY, startIndex, null);
			SearchResult results = searchResult.claim();

			for (Issue issue : results.getIssues()) {
				if (taskFilter.isSame(ticket, issue)) {
					return true;
				}
			}

			if (startIndex >= results.getTotal()) {
				break;
			}

			startIndex += MAX_PER_QUERY;
		}

		return false;
	}
}
