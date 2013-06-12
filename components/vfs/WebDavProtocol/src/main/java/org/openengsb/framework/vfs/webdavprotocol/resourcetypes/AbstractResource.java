package org.openengsb.framework.vfs.webdavprotocol.resourcetypes;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
import io.milton.http.Auth;
import io.milton.http.Request;
import io.milton.http.Request.Method;
import io.milton.http.http11.auth.DigestGenerator;
import io.milton.http.http11.auth.DigestResponse;
import io.milton.resource.DigestResource;
import io.milton.resource.PropFindableResource;
import java.util.Date;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.openengsb.core.api.AliveState;
import org.openengsb.core.api.security.Credentials;
import org.openengsb.core.api.security.model.Authentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openengsb.domain.authentication.*;
import org.openengsb.domain.authorization.*;
import org.openengsb.core.services.*;

/**
 *
 *
 */
public abstract class AbstractResource implements DigestResource, PropFindableResource
{

	private Logger log = LoggerFactory.getLogger(AbstractResource.class);
	private OpenEngSBShiroAuthenticator openEngSBShiroAuthenticator = new OpenEngSBShiroAuthenticator();

	public AbstractResource()
	{
	}

	@Override
	public Object authenticate(String user, String requestedPassword)
	{	
		UsernamePasswordToken token = new UsernamePasswordToken(user, requestedPassword);
		AuthenticationInfo info;
		try
		{
			info = openEngSBShiroAuthenticator.authenticate(token);
		}
		catch (Exception ex)
		{
			return null;
		}
		/*
		 if (user.equals("user") && requestedPassword.equals("password"))
		 {
		 return user;
		 }
		 */
		return info;
	}

	@Override
	public Object authenticate(DigestResponse digestRequest)
	{	
		if (digestRequest.getUser().equals("user"))
		{
			DigestGenerator gen = new DigestGenerator();
			String actual = gen.generateDigest(digestRequest, "password");
			if (actual.equals(digestRequest.getResponseDigest()))
			{
				return digestRequest.getUser();
			}
			else
			{
				log.warn("that password is incorrect. Try 'password'");
			}
		}
		else
		{
			log.warn("user not found: " + digestRequest.getUser() + " - try 'userA'");
		}
		return null;

	}

	@Override
	public String checkRedirect(Request request)
	{
		return null;
	}

	@Override
	public boolean authorise(Request request, Method method, Auth auth)
	{
		log.debug("authorise");
		return auth != null;
	}

	@Override
	public String getRealm()
	{
		return "testrealm@host.com";
	}

	@Override
	public Date getCreateDate()
	{
		return null;
	}

	@Override
	public boolean isDigestAllowed()
	{
		return false;
	}
}