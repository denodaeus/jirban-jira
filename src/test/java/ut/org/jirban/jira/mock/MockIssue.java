/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2016, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package ut.org.jirban.jira.mock;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import org.ofbiz.core.entity.GenericValue;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.attachment.Attachment;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.renderer.IssueRenderContext;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.label.Label;
import com.atlassian.jira.issue.priority.Priority;
import com.atlassian.jira.issue.resolution.Resolution;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.version.Version;

/**
 * Mockito seems to get confused with deep mocks, so hardcode these value objects
 *
 * @author Kabir Khan
 */
public class MockIssue implements Issue {

    private final String key;
    private final IssueType issueType;
    private final Priority priority;
    private final String summary;
    private final User assignee;
    private final Set<ProjectComponent> components;
    private final Status state;

    private final Map<Long, Object> customFields = new HashMap<>();

    public MockIssue(String key, IssueType issueType, Priority priority, String summary, User assignee,
                     Set<ProjectComponent> components, Status state) {
        this.key = key;
        this.issueType = issueType;
        this.priority = priority;
        this.summary = summary;
        this.assignee = assignee;
        this.components = components;
        this.state = state;
    }

    @Override
    public Long getId() {
        return null;
    }

    @Override
    public GenericValue getProject() {
        return null;
    }

    @Override
    public Project getProjectObject() {
        return null;
    }

    @Override
    public Long getProjectId() {
        return null;
    }

    @Override
    public GenericValue getIssueType() {
        return null;
    }

    @Override
    public IssueType getIssueTypeObject() {
        return issueType;
    }

    @Override
    public String getIssueTypeId() {
        return issueType.getId();
    }

    @Override
    public String getSummary() {
        return summary;
    }

    @Override
    public User getAssigneeUser() {
        return assignee;
    }

    @Override
    public User getAssignee() {
        return assignee;
    }

    @Override
    public String getAssigneeId() {
        return assignee.getName();
    }

    @Override
    public Collection<GenericValue> getComponents() {
        return null;
    }

    @Override
    public Collection<ProjectComponent> getComponentObjects() {
        return components;
    }

    @Override
    public User getReporterUser() {
        return null;
    }

    @Override
    public User getReporter() {
        return null;
    }

    @Override
    public String getReporterId() {
        return null;
    }

    @Override
    public User getCreator() {
        return null;
    }

    @Override
    public String getCreatorId() {
        return null;
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public String getEnvironment() {
        return null;
    }

    @Override
    public Collection<Version> getAffectedVersions() {
        return null;
    }

    @Override
    public Collection<Version> getFixVersions() {
        return null;
    }

    @Override
    public Timestamp getDueDate() {
        return null;
    }

    @Override
    public GenericValue getSecurityLevel() {
        return null;
    }

    @Override
    public Long getSecurityLevelId() {
        return null;
    }

    @Nullable
    @Override
    public GenericValue getPriority() {
        return null;
    }

    @Nullable
    @Override
    public Priority getPriorityObject() {
        return priority;
    }

    @Override
    public String getResolutionId() {
        return null;
    }

    @Override
    public GenericValue getResolution() {
        return null;
    }

    @Override
    public Resolution getResolutionObject() {
        return null;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public Long getNumber() {
        return null;
    }

    @Override
    public Long getVotes() {
        return null;
    }

    @Override
    public Long getWatches() {
        return null;
    }

    @Override
    public Timestamp getCreated() {
        return null;
    }

    @Override
    public Timestamp getUpdated() {
        return null;
    }

    @Override
    public Timestamp getResolutionDate() {
        return null;
    }

    @Override
    public Long getWorkflowId() {
        return null;
    }

    @Override
    public Object getCustomFieldValue(CustomField customField) {
        return customFields.get(customField.getIdAsLong());
    }

    @Override
    public GenericValue getStatus() {
        return null;
    }

    @Override
    public String getStatusId() {
        return state.getId();
    }

    @Override
    public Status getStatusObject() {
        return state;
    }

    @Override
    public Long getOriginalEstimate() {
        return null;
    }

    @Override
    public Long getEstimate() {
        return null;
    }

    @Override
    public Long getTimeSpent() {
        return null;
    }

    @Override
    public Object getExternalFieldValue(String s) {
        return null;
    }

    @Override
    public boolean isSubTask() {
        return false;
    }

    @Override
    public Long getParentId() {
        return null;
    }

    @Override
    public boolean isCreated() {
        return false;
    }

    @Override
    public Issue getParentObject() {
        return null;
    }

    @Override
    public GenericValue getParent() {
        return null;
    }

    @Override
    public Collection<GenericValue> getSubTasks() {
        return null;
    }

    @Override
    public Collection<Issue> getSubTaskObjects() {
        return null;
    }

    @Override
    public boolean isEditable() {
        return false;
    }

    @Override
    public IssueRenderContext getIssueRenderContext() {
        return null;
    }

    @Override
    public Collection<Attachment> getAttachments() {
        return null;
    }

    @Override
    public Set<Label> getLabels() {
        return null;
    }

    @Override
    public GenericValue getGenericValue() {
        return null;
    }

    @Override
    public String getString(String s) {
        return null;
    }

    @Override
    public Timestamp getTimestamp(String s) {
        return null;
    }

    @Override
    public Long getLong(String s) {
        return null;
    }

    @Override
    public void store() {

    }

    void setCustomField(Long customFieldId, Object value) {
        if (value == null) {
            customFields.remove(customFieldId);
        } else {
            customFields.put(customFieldId, value);
        }
    }
}
