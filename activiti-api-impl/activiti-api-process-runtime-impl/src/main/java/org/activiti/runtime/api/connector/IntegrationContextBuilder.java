/*
 * Copyright 2018 Alfresco, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.runtime.api.connector;

import org.activiti.api.process.model.IntegrationContext;
import org.activiti.api.runtime.model.impl.IntegrationContextImpl;
import org.activiti.bpmn.model.ServiceTask;
import org.activiti.core.common.model.connector.ActionDefinition;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.impl.context.ExecutionContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.integration.IntegrationContextEntity;
import org.activiti.engine.repository.ProcessDefinition;

public class IntegrationContextBuilder {

    private InboundVariablesProvider inboundVariablesProvider;

    public IntegrationContextBuilder(InboundVariablesProvider inboundVariablesProvider) {
        this.inboundVariablesProvider = inboundVariablesProvider;
    }

    public IntegrationContext from(IntegrationContextEntity integrationContextEntity,
                                   DelegateExecution execution, ActionDefinition actionDefinition) {
        IntegrationContextImpl integrationContext = buildFromExecution(execution, actionDefinition);
        integrationContext.setId(integrationContextEntity.getId());
        return integrationContext;
    }

    public IntegrationContext from(DelegateExecution execution, ActionDefinition actionDefinition) {
        return buildFromExecution(execution,
                                  actionDefinition);
    }

    private IntegrationContextImpl buildFromExecution(DelegateExecution execution,
                                                      ActionDefinition actionDefinition) {
        IntegrationContextImpl integrationContext = new IntegrationContextImpl();
        integrationContext.setProcessInstanceId(execution.getProcessInstanceId());
        integrationContext.setProcessDefinitionId(execution.getProcessDefinitionId());
        integrationContext.setBusinessKey(execution.getProcessInstanceBusinessKey());
        integrationContext.setClientId(execution.getCurrentActivityId());

        if(ExecutionEntity.class.isInstance(execution)) {
            ExecutionContext executionContext = new ExecutionContext(ExecutionEntity.class.cast(execution));

            ExecutionEntity processInstance = executionContext.getProcessInstance(); 

            if(processInstance != null) {
                integrationContext.setParentProcessInstanceId(processInstance.getParentProcessInstanceId());
            }
            
            // Let's try to resolve process definition attributes  
            ProcessDefinition processDefinition = executionContext.getProcessDefinition();

            if(processDefinition != null) {
                integrationContext.setProcessDefinitionKey(processDefinition.getKey());
                integrationContext.setProcessDefinitionVersion(processDefinition.getVersion());
            }
            
        }

        ServiceTask serviceTask = (ServiceTask)execution.getCurrentFlowElement();
        if (serviceTask != null ) {
            integrationContext.setConnectorType(serviceTask.getImplementation());
            integrationContext.setClientName(serviceTask.getName());
            integrationContext.setClientType(ServiceTask.class.getSimpleName());
        }


        integrationContext.setInBoundVariables(inboundVariablesProvider.calculateVariables(execution, actionDefinition));

        return integrationContext;
    }

}
