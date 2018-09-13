package com.atlassian.plugins.tutorial.jira.reports;

import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.datetime.DateTimeFormatter;
import com.atlassian.jira.datetime.DateTimeFormatterFactory;
import com.atlassian.jira.datetime.DateTimeStyle;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.plugin.report.impl.AbstractReport;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.ParameterUtils;
import com.atlassian.jira.web.action.ProjectActionSupport;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.JiraImport;
import com.atlassian.query.Query;
import org.apache.commons.lang3.time.DateUtils;

import java.util.*;

@Scanned
public class IssueDateFilterReport extends AbstractReport {

    @JiraImport
    private final SearchService searchService;
    @JiraImport
    private final DateTimeFormatter formatter;

    private Date dueDate;

    public IssueDateFilterReport(SearchService searchService,  @JiraImport DateTimeFormatterFactory dateTimeFormatterFactory) {
        this.searchService = searchService;
        this.formatter = dateTimeFormatterFactory.formatter().withStyle(DateTimeStyle.DATE).forLoggedInUser();
    }

    public String generateReportHtml(ProjectActionSupport action, Map params) throws SearchException {
        return descriptor.getHtml("view", new HashMap<String, Object>() {{
            put("dueDate", formatter.format(dueDate));
            put("issues", getIssuesBeforeDueDate(action.getLoggedInUser(), dueDate));
        }});
    }

    private List<Issue> getIssuesBeforeDueDate(ApplicationUser user, Date dueDate) throws SearchException {
        Query query = JqlQueryBuilder.newBuilder().where().due().lt(dueDate).buildQuery();
        return searchService.search(user, query, PagerFilter.getUnlimitedFilter()).getIssues();
    }

    public void validate(ProjectActionSupport action, Map params) {
        try {
            if (ParameterUtils.getStringParam(params, "dueDate").isEmpty()) {
                //not working without date truncate
                dueDate = DateUtils.truncate(new Date(), Calendar.DATE);
            } else {
                dueDate = formatter.parse(ParameterUtils.getStringParam(params, "dueDate"));
            }
        } catch (IllegalArgumentException ex) {
            action.addError("dueDate", action.getText("report.issuedatefilterreport.duedate.invalid"));
        }
    }
}
