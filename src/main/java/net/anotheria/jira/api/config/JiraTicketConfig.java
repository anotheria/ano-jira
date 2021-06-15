package net.anotheria.jira.api.config;

import org.configureme.ConfigurationManager;
import org.configureme.annotations.Configure;
import org.configureme.annotations.ConfigureMe;
import org.configureme.annotations.DontConfigure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

@ConfigureMe(name = "jira-ticket-config")
public class JiraTicketConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(JiraTicketConfig.class);

    @DontConfigure
    private static JiraTicketConfig INSTANCE;

    @Configure
    private boolean enabled;

    @Configure
    private String baseJiraUrl;

    @Configure
    private String jiraMasterAcc;

    @Configure
    private String jiraMasterPass;

    @Configure
    private String projectKey;

    @Configure
    private String epicName;

    @Configure
    private String epicKey;

    @Configure
    private String defaultSummaryPrefix = "Error ";

    @Configure
    private Long defaultPriority = 2L;

    @Configure
    private String defaultReporterName;

    @Configure
    private String defaultAssigneeName;

    @Configure
    private String[] labels;

    /**
     * Default constructor.
     */
    private JiraTicketConfig() {

        try {
            ConfigurationManager.INSTANCE.configure(this);
        } catch (final IllegalArgumentException e) {
            LOGGER.warn("Configuration fail[" + e.getMessage() + "]. Relaying on defaults.");
        }
    }

    /**
     * Returns instance of {@link JiraTicketConfig}.
     */
    public static JiraTicketConfig getInstance() {

        if (INSTANCE == null) {

            synchronized (JiraTicketConfig.class) {

                if (INSTANCE == null) {
                    INSTANCE = new JiraTicketConfig();
                }
            }
        }

        return INSTANCE;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getBaseJiraUrl() {
        return baseJiraUrl;
    }

    public void setBaseJiraUrl(String baseJiraUrl) {
        this.baseJiraUrl = baseJiraUrl;
    }

    public String getJiraMasterAcc() {
        return jiraMasterAcc;
    }

    public void setJiraMasterAcc(String jiraMasterAcc) {
        this.jiraMasterAcc = jiraMasterAcc;
    }

    public String getJiraMasterPass() {
        return jiraMasterPass;
    }

    public void setJiraMasterPass(String jiraMasterPass) {
        this.jiraMasterPass = jiraMasterPass;
    }

    public String getProjectKey() {
        return projectKey;
    }

    public void setProjectKey(String projectKey) {
        this.projectKey = projectKey;
    }

    public String getEpicName() {
        return epicName;
    }

    public void setEpicName(String epicName) {
        this.epicName = epicName;
    }

    public String getEpicKey() {
        return epicKey;
    }

    public void setEpicKey(String epicKey) {
        this.epicKey = epicKey;
    }

    public String getDefaultSummaryPrefix() {
        return defaultSummaryPrefix;
    }

    public void setDefaultSummaryPrefix(String defaultSummaryPrefix) {
        this.defaultSummaryPrefix = defaultSummaryPrefix;
    }

    public Long getDefaultPriority() {
        return defaultPriority;
    }

    public void setDefaultPriority(Long defaultPriority) {
        this.defaultPriority = defaultPriority;
    }

    public String getDefaultReporterName() {
        return defaultReporterName;
    }

    public void setDefaultReporterName(String defaultReporterName) {
        this.defaultReporterName = defaultReporterName;
    }

    public String getDefaultAssigneeName() {
        return defaultAssigneeName;
    }

    public void setDefaultAssigneeName(String defaultAssigneeName) {
        this.defaultAssigneeName = defaultAssigneeName;
    }

    public String[] getLabels() {
        return labels;
    }

    public void setLabels(String[] labels) {
        this.labels = labels;
    }

    @Override
    public String toString() {
        return "JiraTicketConfig{" +
                "enabled=" + enabled +
                ", baseJiraUrl='" + baseJiraUrl + '\'' +
                ", jiraMasterAcc='" + jiraMasterAcc + '\'' +
                ", jiraMasterPass='" + jiraMasterPass + '\'' +
                ", projectKey='" + projectKey + '\'' +
                ", epicKey='" + epicKey + '\'' +
                ", defaultSummaryPrefix='" + defaultSummaryPrefix + '\'' +
                ", defaultPriority=" + defaultPriority +
                ", defaultReporterName='" + defaultReporterName + '\'' +
                ", defaultAssigneeName='" + defaultAssigneeName + '\'' +
                ", labels=" + Arrays.toString(labels) +
                '}';
    }
}
