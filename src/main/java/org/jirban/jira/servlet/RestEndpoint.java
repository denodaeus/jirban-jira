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
package org.jirban.jira.servlet;

import static org.jirban.jira.impl.Constants.BOARDS;
import static org.jirban.jira.impl.Constants.HELP;
import static org.jirban.jira.impl.Constants.ISSUES;
import static org.jirban.jira.impl.Constants.JIRBAN_VERSION;
import static org.jirban.jira.impl.Constants.UPDATES;
import static org.jirban.jira.impl.Constants.VERSION;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.dmr.ModelNode;
import org.jirban.jira.JirbanValidationException;
import org.jirban.jira.api.JiraFacade;
import org.jirban.jira.impl.Constants;
import org.jirban.jira.impl.board.RawSqlLoader;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;

/**
 * @author Kabir Khan
 */
@Path("/")
@Consumes({MediaType.APPLICATION_JSON})
@Produces({MediaType.APPLICATION_JSON})
public class RestEndpoint {

    /**
     * If we change anything in the payloads etc. we should bump this so that the client can take action.
     * The corresponding location on the client is in app.ts
     */
    private static final int API_VERSION = 1;

    private final JiraFacade jiraFacade;

    @Inject
    public RestEndpoint(JiraFacade jiraFacade) {
        this.jiraFacade = jiraFacade;
    }

    @GET
    @Path(VERSION)
    public Response getVersion() {
        ModelNode versionNode = new ModelNode();
        //Remove this later
        versionNode.get(VERSION).set(API_VERSION);
        versionNode.get(Constants.API_VERSION).set(API_VERSION);
        versionNode.get(JIRBAN_VERSION).set(jiraFacade.getJirbanVersion());
        return createResponse(versionNode);
    }

    @GET
    @Path(BOARDS)
    public Response getBoards(@QueryParam("full") Boolean full) {
        final String json;
        if (full != null && full.booleanValue()) {
            json = jiraFacade.getBoardConfigurations(getUser());
        } else {
            json = jiraFacade.getBoardsForDisplay(getUser());
        }
        return createResponse(json);
    }

    @GET
    @Path(ISSUES + "/{boardCode}")
    public Response getBoard(
            @PathParam("boardCode") String boardCode,
            @QueryParam("backlog") Boolean backlog) throws SearchException {
        //TODO figure out if a permission violation becomes a search exception
        return createResponse(
                jiraFacade.getBoardJson(
                        getUser(),
                        backlog != null && backlog.booleanValue(),
                        boardCode));
    }

    @GET
    @Path(ISSUES + "/{boardCode}/" + UPDATES + "/{viewId}")
    public Response getBoard(@PathParam("boardCode") String boardCode,
                              @PathParam("viewId") int viewId,
                              @QueryParam("backlog") Boolean backlog) throws SearchException {
        //TODO figure out if a permission violation becomes a search exception
        return createResponse(
                jiraFacade.getChangesJson(getUser(),
                        backlog != null && backlog.booleanValue(),
                        boardCode,
                        viewId));
    }

    @GET
    @Path(ISSUES + "/{boardCode}/" + HELP)
    public Response getBoard(
            @PathParam("boardCode") String boardCode) throws SearchException {
        //TODO figure out if a permission violation becomes a search exception
        return createResponse(
                jiraFacade.getStateHelpTexts(getUser(), boardCode));
    }



    @GET
    @Path(BOARDS + "/{boardId}")
    public Response getBoardConfig(@PathParam("boardId") int boardId) {
        ApplicationUser user = getUser();
        String json = jiraFacade.getBoardJsonForConfig(user, boardId);
        return createResponse(json);
    }

    @DELETE
    @Path(BOARDS + "/{boardId}")
    public Response deleteBoard(@PathParam("boardId") int boardId) {
        ApplicationUser user = getUser();
        jiraFacade.deleteBoardConfiguration(user, boardId);
        String json = jiraFacade.getBoardConfigurations(user);
        return createResponse(json);
    }

    @POST
    @Path(BOARDS)
    public Response createBoard(@Context HttpServletRequest req, String config) {
        ApplicationUser user = getUser();
        jiraFacade.saveBoardConfiguration(user, -1, Util.getDeployedUrl(req), ModelNode.fromJSONString(config));
        String json = jiraFacade.getBoardConfigurations(user);
        return createResponse(json);
    }

    @PUT
    @Path(BOARDS + "/{boardId}")
    public Response saveBoard(
            @Context HttpServletRequest req,
            @PathParam("boardId") int boardId,
            String config) {
        ApplicationUser user = getUser();
        jiraFacade.saveBoardConfiguration(user, boardId, Util.getDeployedUrl(req), ModelNode.fromJSONString(config));
        String json = jiraFacade.getBoardConfigurations(user);
        return createResponse(json);
    }

    @PUT
    @Path("rankCustomFieldId")
    public Response saveCustomFieldId(String value) {
        ApplicationUser user = getUser();
        jiraFacade.saveCustomFieldId(user, ModelNode.fromJSONString(value));
        String json = "{}";
        return createResponse(json);
    }

    @POST
    @Path("db-explorer")
    public Response executeSql(@Context HttpServletRequest req, String queryJson) {
        ApplicationUser user = getUser();

        //Some lockdown of urls and users
        boolean valid = false;
        final String host = req.getHeader("host");
        if (host != null) {
            if (host.startsWith("localhost")) {
                if (user.getKey().equals("admin")) {
                    valid = true;
                }
            } else if (host.equals("issues.stage.jboss.org")) {
                if (user.getKey().equals("kabirkhan")) {
                    valid = true;
                }
            }
        }
        if (!valid) {
            throw new JirbanValidationException("You are not allowed to execute sql queries on this server");
        }

        ModelNode node = ModelNode.fromJSONString(queryJson);
        String sql = node.get("sql").asString().trim();
        if (!sql.toUpperCase().startsWith("SELECT")) {
            throw new JirbanValidationException("Only select statements are allowed");
        }

        //TODO configure this if needed
        RawSqlLoader loader = RawSqlLoader.create("defaultDS");
        return createResponse(loader.executeQuery(sql).toJSONString(true));
    }


    private Response createResponse(ModelNode modelNode) {
        return createResponse(modelNode.toJSONString(true));
    }

    private Response createResponse(String json) {
        return Response.ok(json).build();
    }

    private ApplicationUser getUser() {
        //Jira doesn't seem to like injection of this
        JiraAuthenticationContext authenticationContext = ComponentAccessor.getJiraAuthenticationContext();
        return authenticationContext.getUser();
    }
}