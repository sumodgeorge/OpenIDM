/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright © 2012 ForgeRock Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at
 * http://forgerock.org/license/CDDLv1.0.html
 * See the License for the specific language governing
 * permission and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at http://forgerock.org/license/CDDLv1.0.html
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 */
package org.forgerock.openidm.workflow.activiti.internal;

import org.forgerock.openidm.workflow.activiti.ActivitiConstants;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.form.DateFormType;
import org.activiti.engine.impl.form.DefaultTaskFormHandler;
import org.activiti.engine.impl.form.EnumFormType;
import org.activiti.engine.impl.form.FormPropertyHandler;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.task.TaskDefinition;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.forgerock.json.fluent.JsonValue;
import org.forgerock.json.resource.*;
import org.forgerock.openidm.workflow.activiti.internal.mixin.DateFormTypeMixIn;
import org.forgerock.openidm.workflow.activiti.internal.mixin.EnumFormTypeMixIn;
import org.forgerock.openidm.workflow.activiti.internal.mixin.TaskDefinitionMixIn;

/**
 * Resource implementation of TaskDefinition related Activiti operations
 * @author orsolyamebold
 */
public class TaskDefinitionResource implements CollectionResourceProvider {

    private final static ObjectMapper mapper;
    private ProcessEngine processEngine;

    static {
        mapper = new ObjectMapper();
        mapper.getSerializationConfig().addMixInAnnotations(TaskDefinition.class, TaskDefinitionMixIn.class);
        mapper.getSerializationConfig().addMixInAnnotations(EnumFormType.class, EnumFormTypeMixIn.class);
        mapper.getSerializationConfig().addMixInAnnotations(DateFormType.class, DateFormTypeMixIn.class);
        mapper.configure(SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS, false);
        mapper.configure(SerializationConfig.Feature.SORT_PROPERTIES_ALPHABETICALLY, true);
    }

    public TaskDefinitionResource(ProcessEngine processEngine) {
        this.processEngine = processEngine;
    }

    public void setProcessEngine(ProcessEngine processEngine) {
        this.processEngine = processEngine;
    }

    @Override
    public void actionCollection(ServerContext context, ActionRequest request, ResultHandler<JsonValue> handler) {
        handler.handleError(new NotSupportedException("ActionCollection on TaskDefinitionResource not supported."));
    }

    @Override
    public void actionInstance(ServerContext context, String resourceId, ActionRequest request, ResultHandler<JsonValue> handler) {
        handler.handleError(new NotSupportedException("ActionInstance on TaskDefinitionResource not supported."));
    }

    @Override
    public void createInstance(ServerContext context, CreateRequest request, ResultHandler<Resource> handler) {
        handler.handleError(new NotSupportedException("CreateInstance on TaskDefinitionResource not supported."));
    }

    @Override
    public void deleteInstance(ServerContext context, String resourceId, DeleteRequest request, ResultHandler<Resource> handler) {
        handler.handleError(new NotSupportedException("DeleteInstance on TaskDefinitionResource not supported."));
    }

    @Override
    public void patchInstance(ServerContext context, String resourceId, PatchRequest request, ResultHandler<Resource> handler) {
        handler.handleError(new NotSupportedException("PatchInstance on TaskDefinitionResource not supported."));
    }

    @Override
    public void queryCollection(ServerContext context, QueryRequest request, QueryResultHandler handler) {
        try {
            if (ActivitiConstants.QUERY_ALL_IDS.equals(request.getQueryId())) {
                String processDefinitionId = ((RouterContext) context).getUriTemplateVariables().get("objid");
                ProcessDefinitionEntity procdef = (ProcessDefinitionEntity) ((RepositoryServiceImpl) processEngine.getRepositoryService()).getDeployedProcessDefinition(processDefinitionId);
                Map<String, TaskDefinition> taskdefinitions = procdef.getTaskDefinitions();
                for (TaskDefinition taskDefinition : taskdefinitions.values()) {
                    DefaultTaskFormHandler taskFormHandler = (DefaultTaskFormHandler) taskDefinition.getTaskFormHandler();
                    Map value = mapper.convertValue(taskDefinition, HashMap.class);
                    Resource r = new Resource(taskDefinition.getKey(), null, new JsonValue(value));
                    r.getContent().add(ActivitiConstants.ACTIVITI_FORMRESOURCEKEY, taskFormHandler.getFormKey());
                    List<Map> propertyList = new ArrayList<Map>();
                    r.getContent().add(ActivitiConstants.FORMPROPERTIES, propertyList);
                    handler.handleResource(r);
                }
                handler.handleResult(new QueryResult());
            } else {
                handler.handleError(new BadRequestException("Unknown query-id"));
            }
        } catch (Exception ex) {
            handler.handleError(new InternalServerErrorException(ex.getMessage(), ex));
        }
    }

    @Override
    public void readInstance(ServerContext context, String resourceId, ReadRequest request, ResultHandler<Resource> handler) {
        try {
            String processDefinitionId = ((RouterContext) context).getUriTemplateVariables().get("objid");
            ProcessDefinitionEntity procdef = (ProcessDefinitionEntity) ((RepositoryServiceImpl) processEngine.getRepositoryService()).getDeployedProcessDefinition(processDefinitionId);
            TaskDefinition taskDefinition = procdef.getTaskDefinitions().get(resourceId);
            DefaultTaskFormHandler taskFormHandler = (DefaultTaskFormHandler) taskDefinition.getTaskFormHandler();
            Map value = mapper.convertValue(taskDefinition, HashMap.class);
            Resource r = new Resource(taskDefinition.getKey(), null, new JsonValue(value));
            r.getContent().add(ActivitiConstants.ACTIVITI_FORMRESOURCEKEY, taskFormHandler.getFormKey());
            List<Map> propertyList = new ArrayList<Map>();
            r.getContent().add(ActivitiConstants.FORMPROPERTIES, propertyList);
            handler.handleResult(r);
        } catch (Exception ex) {
            handler.handleError(new InternalServerErrorException(ex.getMessage(), ex));
        }
    }

    @Override
    public void updateInstance(ServerContext context, String resourceId, UpdateRequest request, ResultHandler<Resource> handler) {
        handler.handleError(new NotSupportedException("UpdateInstance on TaskDefinitionResource not supported."));
    }
}
