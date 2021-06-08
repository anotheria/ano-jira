package net.anotheria.jira.api;

import net.anotheria.anoplass.api.APIFactory;
import net.anotheria.jira.api.filter.JiraCreateTicketByExceptionFilter;

/**
 * Jira API factory
 */
public class JiraAPIFactory implements APIFactory<JiraAPI> {

    private JiraCreateTicketByExceptionFilter taskFilter;

    public JiraAPIFactory(JiraCreateTicketByExceptionFilter taskFilter) {
        this.taskFilter = taskFilter;
    }

    @Override
    public JiraAPI createAPI() {
        return new JiraAPIImpl(taskFilter);
    }
}
