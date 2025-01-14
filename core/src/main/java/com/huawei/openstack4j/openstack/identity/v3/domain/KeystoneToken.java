/*******************************************************************************
 * 	Copyright 2016 ContainX and OpenStack4j                                          
 * 	                                                                                 
 * 	Licensed under the Apache License, Version 2.0 (the "License"); you may not      
 * 	use this file except in compliance with the License. You may obtain a copy of    
 * 	the License at                                                                   
 * 	                                                                                 
 * 	    http://www.apache.org/licenses/LICENSE-2.0                                   
 * 	                                                                                 
 * 	Unless required by applicable law or agreed to in writing, software              
 * 	distributed under the License is distributed on an "AS IS" BASIS, WITHOUT        
 * 	WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the         
 * 	License for the specific language governing permissions and limitations under    
 * 	the License.                                                                     
 *******************************************************************************/
/******************************************************************************* 	                                                                                 
 *  Huawei has modified this source file.
 * 	Copyright 2018 Huawei Technologies Co.,Ltd.                                         
 * 	                                                                                 
 * 	Licensed under the Apache License, Version 2.0 (the "License"); you may not      
 * 	use this file except in compliance with the License. You may obtain a copy of    
 * 	the License at                                                                   
 * 	                                                                                 
 * 	    http://www.apache.org/licenses/LICENSE-2.0                                   
 * 	                                                                                 
 * 	Unless required by applicable law or agreed to in writing, software              
 * 	distributed under the License is distributed on an "AS IS" BASIS, WITHOUT        
 * 	WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the         
 * 	License for the specific language governing permissions and limitations under    
 * 	the License.                             
 * *******************************************************************************/
package com.huawei.openstack4j.openstack.identity.v3.domain;

import static com.huawei.openstack4j.openstack.identity.functions.ServiceFunctions.*;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.huawei.openstack4j.model.identity.AuthStore;
import com.huawei.openstack4j.model.identity.AuthVersion;
import com.huawei.openstack4j.model.identity.v3.Project;
import com.huawei.openstack4j.model.identity.v3.Service;
import com.huawei.openstack4j.model.identity.v3.Token;

import com.google.common.base.MoreObjects;
import com.google.common.collect.SortedSetMultimap;
import com.google.common.collect.TreeMultimap;

@JsonRootName("token")
@JsonIgnoreProperties(ignoreUnknown = true)
public class KeystoneToken implements Token {

    private static final long serialVersionUID = 1L;

    @JsonIgnore
    private String id;
    @JsonProperty("expires_at")
    private Date expires;
    @JsonProperty("issued_at")
    private Date issued;
    @JsonProperty("audit_ids")
    private List<String> auditIds;
    @JsonProperty
    private List<String> methods;
    @JsonProperty
    private List<KeystoneRole> roles;
    @JsonProperty(required = false)
    private KeystoneProject project;
    @JsonProperty(required = false)
    private KeystoneDomain domain;
    @JsonProperty
    private KeystoneUser user;
    @JsonProperty
    private List<KeystoneService> catalog;
    private KeystoneAuth credentials;
    private String endpoint;
    @JsonIgnore
    private String extras;
    @JsonIgnore
    private String authType;
    @JsonIgnore
    private volatile SortedSetMultimap<String, Service> aggregatedCatalog;

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).omitNullValues()
                .add("id", id)
                .add("expires", expires)
                .add("issued", issued)
                .add("auditIds", auditIds)
                .add("methods", methods)
                .add("user", user)
                .add("roles", roles)
                .add("project", project)
                .add("domain", domain)
                .addValue("\n")
                .add("catalog", catalog)
                .toString();
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public Date getExpires() {
        return expires;
    }

    @Override
    public AuthVersion getVersion() {
        return AuthVersion.V3;
    }

    @Override
    public Date getIssuedAt() {
        return issued;
    }

    @Override
    public List<String> getAuditIds() {
        return auditIds;
    }

    @Override
    public List<String> getMethods() {
        return methods;
    }

    @Override
    public List<? extends KeystoneService> getCatalog() {
        return catalog;
    }

    @Override
    public KeystoneProject getProject() {
        return project;
    }

    @Override
    public KeystoneDomain getDomain() {
        return domain;
    }

    @Override
    public KeystoneUser getUser() {
        return user;
    }

    @Override
    public List<? extends KeystoneRole> getRoles() {
        return roles;
    }

    @Override
    public KeystoneAuth getCredentials() {
        return credentials;
    }

    @Override
    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getAuthType() {
        return authType;
    }

    public void setAuthType(String authType) {
        this.authType = authType;
    }

    public void setCatalog(List<KeystoneService> catalog){
        this.catalog = catalog;
    }

    public KeystoneToken applyContext(String endpoint, KeystoneAuth credentials) {
        this.credentials = credentials;
        this.endpoint = endpoint;
        return this;
    }

    public KeystoneToken applyContext(String endpoint, TokenAuth token) {
        this.endpoint = endpoint;
        return this;
    }

    public KeystoneToken applyContext(String endpoint, AuthStore credentials) {
        this.endpoint = endpoint;
        this.credentials = new KeystoneAuth(endpoint, credentials.getPassword());
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @JsonIgnore
    public SortedSetMultimap<String, Service> getAggregatedCatalog() {
        if (aggregatedCatalog == null) {
            synchronized(this) {
                if (aggregatedCatalog == null) {
                    aggregatedCatalog = TreeMultimap.create();
                    for (Service sc : catalog) {
                        String nameKey = TYPE_WITHOUT_VERSION.apply(sc.getName());
                        String typeKey = TYPE_WITHOUT_VERSION.apply(sc.getType());
                        aggregatedCatalog.put(nameKey, sc);
                        aggregatedCatalog.put(typeKey, sc);
                    }
                }
            }
        }
        return aggregatedCatalog;
    }

    @JsonIgnore
    @Override
    public String getCacheIdentifier() {
        String uniq = null;
        if (getProject() != null)
            uniq = getProject().getId();
        else if (getDomain() != null)
            uniq = getDomain().getId();
        else if (user != null)
            uniq = user.getId();
        else
            uniq = "";
        return String.format("%s:%s", endpoint, uniq);

    }

	@Override
	public void setProjectInfo(Project project) {
		this.project=(KeystoneProject) project;
	}
}
