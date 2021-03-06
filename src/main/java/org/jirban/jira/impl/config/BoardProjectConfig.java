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
package org.jirban.jira.impl.config;

import static org.jirban.jira.impl.Constants.COLOUR;
import static org.jirban.jira.impl.Constants.CUSTOM;
import static org.jirban.jira.impl.Constants.STATE_LINKS;
import static org.jirban.jira.impl.config.Util.getRequiredChild;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;
import org.jboss.dmr.Property;
import org.jirban.jira.JirbanLogger;
import org.jirban.jira.JirbanValidationException;
import org.jirban.jira.impl.Constants;

/** Abstract base class for project configurations of projects whose issues should appear as cards on the board.
 * @author Kabir Khan
 */
public class BoardProjectConfig extends ProjectConfig {
    private final BoardStates boardStates;
    private final String queryFilter;
    private final String colour;
    private final Map<String, String> ownToBoardStates;
    /** Maps the owner states onto our states */
    private final Map<String, String> boardToOwnStates;

    private final Set<String> ownDoneStateNames;

    private final List<String> customFieldNames;


    private BoardProjectConfig(final BoardStates boardStates,
                                final String code, final String queryFilter,
                                final String colour, final Map<String, Integer> states,
                                final Map<String, String> ownToBoardStates,
                                final Map<String, String> boardToOwnStates,
                                final List<String> customFieldNames) {
        super(code, states);
        this.boardStates = boardStates;
        this.queryFilter = queryFilter;
        this.colour = colour;
        this.boardToOwnStates = boardToOwnStates;
        this.ownToBoardStates = ownToBoardStates;

        Set<String> ownDoneStateNames = new HashSet<>();
        for (String boardDoneState : boardStates.getDoneStates()) {
            String ownDoneState = boardToOwnStates.get(boardDoneState);
            if (ownDoneState != null) {
                ownDoneStateNames.add(ownDoneState);
            }
        }
        this.ownDoneStateNames = Collections.unmodifiableSet(ownDoneStateNames);
        this.customFieldNames = customFieldNames;
    }

    static BoardProjectConfig load(final BoardStates boardStates, final String projectCode, ModelNode project, Map<String, CustomFieldConfig> customFieldConfigs) {
        String colour = getRequiredChild(project, "Project", projectCode, COLOUR).asString();
        ModelNode statesLinks = getRequiredChild(project, "Project", projectCode, STATE_LINKS);

        Map<String, String> ownToBoardStates = new LinkedHashMap<>();
        Map<String, String> boardToOwnStates = new HashMap<>();
        for (Property prop : statesLinks.asPropertyList()) {
            final String ownState = prop.getName();
            final String boardState = prop.getValue().asString();
            ownToBoardStates.put(ownState, boardState);
            boardToOwnStates.put(boardState, ownState);
        }

        int i = 0;
        Map<String, Integer> states = new LinkedHashMap<>();
        for (String boardState : boardStates.getStateNames()) {
            final String ownState = boardToOwnStates.get(boardState);
            if (ownState != null) {
                states.put(ownState, i++);
            }
        }


        final List<String> customFieldNames;
        if (!project.hasDefined(CUSTOM)) {
            customFieldNames = Collections.emptyList();
        } else {
            customFieldNames = new ArrayList<>();
            final ModelNode customFieldNode = project.get(CUSTOM);
            if (customFieldNode.getType() != ModelType.LIST) {
                throw new JirbanValidationException("The \"config\" element of project \"" + projectCode + "\" must be an array of strings");
            }
            for (ModelNode field : customFieldNode.asList()) {
                final String fieldName = field.asString();
                if (!customFieldConfigs.containsKey(fieldName)) {
                    throw new JirbanValidationException("The \"config\" element of project \"" + projectCode + "\" contains \"" + fieldName + "\", which does not exist in the board's \"config\" section.");
                }
                customFieldNames.add(fieldName);
            }
        }

        return new BoardProjectConfig(boardStates, projectCode, loadQueryFilter(project), colour,
                Collections.unmodifiableMap(states),
                Collections.unmodifiableMap(ownToBoardStates),
                Collections.unmodifiableMap(boardToOwnStates),
                Collections.unmodifiableList(customFieldNames));
    }


    static String loadQueryFilter(ModelNode project) {
        if (!project.hasDefined(Constants.QUERY_FILTER)) {
            return null;
        }
        String filter = project.get(Constants.QUERY_FILTER).asString().trim();
        if (filter.length() == 0) {
            return null;
        }
        return filter;
    }

    public String getQueryFilter() {
        return queryFilter;
    }

    public String getColour() {
        return colour;
    }


    public Integer mapOwnStateOntoBoardStateIndex(String state) {
        String boardState = mapOwnStateOntoBoardState(state);
        return boardStates.getStateIndex(boardState);

    }
    public String mapBoardStateOntoOwnState(String boardState) {
        return boardToOwnStates.get(boardState);
    }

    public String mapOwnStateOntoBoardState(String state) {
        return ownToBoardStates.get(state);
    }

    @Override
    ModelNode serializeModelNodeForBoard(BoardConfig boardConfig, ModelNode parent) {
        ModelNode projectNode = super.serializeModelNodeForBoard(boardConfig, parent);
        ModelNode stateLinksNode = projectNode.get(STATE_LINKS);
        for (String state : boardStates.getStateNames()) {
            String myState = mapBoardStateOntoOwnState(state);
            stateLinksNode.get(state).set(myState == null ? new ModelNode() : new ModelNode(myState));
        }
        projectNode.get(Constants.COLOUR).set(colour);
        return projectNode;
    }

    ModelNode serializeModelNodeForConfig() {
        final ModelNode projectNode = new ModelNode();
        projectNode.get(Constants.QUERY_FILTER).set(queryFilter == null ? new ModelNode() : new ModelNode(queryFilter));
        projectNode.get(Constants.COLOUR).set(colour);

        if (customFieldNames.size() > 0) {
            final ModelNode customFieldsNode = projectNode.get(CUSTOM);
            for (String customFieldName : customFieldNames) {
                customFieldsNode.add(customFieldName);
            }
        }

        final ModelNode stateLinksNode = projectNode.get(STATE_LINKS);
        stateLinksNode.setEmptyObject();
        for (Map.Entry<String, String> entry : ownToBoardStates.entrySet()) {
            stateLinksNode.get(entry.getKey()).set(entry.getValue());
        }
        return projectNode;
    }

    public boolean isBacklogState(String ownState) {
        return isBacklogState(mapOwnStateOntoBoardStateIndex(ownState));
    }

    public boolean isDoneState(String ownState) {
        Integer boardStateIndex = mapOwnStateOntoBoardStateIndex(ownState);
        return boardStateIndex == null ? false : isDoneState(boardStateIndex);
    }

    private boolean isBacklogState(int boardStateIndex) {
        return boardStates.isBacklogState(boardStateIndex);
    }

    public boolean isUnorderedState(int boardStateIndex) {
        return boardStates.isUnorderedState(boardStateIndex);
    }

    public boolean isDoneState(int boardStateIndex) {
        return boardStates.isDoneState(boardStateIndex);
    }

    public Set<String> getOwnDoneStateNames() {
        return ownDoneStateNames;
    }

    public List<String> getCustomFieldNames() {
        JirbanLogger.LOGGER.trace("Custom fields for project {} are {}", getCode(), customFieldNames);
        return customFieldNames;
    }
}
