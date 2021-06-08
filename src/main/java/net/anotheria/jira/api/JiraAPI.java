package net.anotheria.jira.api;

import net.anotheria.anoplass.api.API;
import net.anotheria.jira.api.bean.JiraExceptionTicketPO;

/**
 * Jira API interface
 */
public interface JiraAPI extends API {

	void createJiraTicket(JiraExceptionTicketPO ticket);

}
