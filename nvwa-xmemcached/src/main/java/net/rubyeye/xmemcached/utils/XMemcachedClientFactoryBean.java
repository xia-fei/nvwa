/**
 *Copyright [2009-2010] [dennis zhuang(killme2008@gmail.com)]
 *Licensed under the Apache License, Version 2.0 (the "License");
 *you may not use this file except in compliance with the License. 
 *You may obtain a copy of the License at 
 *             http://www.apache.org/licenses/LICENSE-2.0 
 *Unless required by applicable law or agreed to in writing, 
 *software distributed under the License is distributed on an "AS IS" BASIS, 
 *WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 *either express or implied. See the License for the specific language governing permissions and limitations under the License
 */
/**
 *Copyright [2009-2010] [dennis zhuang(killme2008@gmail.com)]
 *Licensed under the Apache License, Version 2.0 (the "License");
 *you may not use this file except in compliance with the License.
 *You may obtain a copy of the License at
 *             http://www.apache.org/licenses/LICENSE-2.0
 *Unless required by applicable law or agreed to in writing,
 *software distributed under the License is distributed on an "AS IS" BASIS,
 *WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 *either express or implied. See the License for the specific language governing permissions and limitations under the License
 */
package net.rubyeye.xmemcached.utils;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.rubyeye.xmemcached.CommandFactory;
import net.rubyeye.xmemcached.MemcachedClient;
import net.rubyeye.xmemcached.MemcachedClientBuilder;
import net.rubyeye.xmemcached.MemcachedSessionLocator;
import net.rubyeye.xmemcached.XMemcachedClientBuilder;
import net.rubyeye.xmemcached.auth.AuthInfo;
import net.rubyeye.xmemcached.buffer.BufferAllocator;
import net.rubyeye.xmemcached.buffer.SimpleBufferAllocator;
import net.rubyeye.xmemcached.command.TextCommandFactory;
import net.rubyeye.xmemcached.impl.ArrayMemcachedSessionLocator;
import net.rubyeye.xmemcached.transcoders.SerializingTranscoder;
import net.rubyeye.xmemcached.transcoders.Transcoder;

import org.springframework.beans.factory.FactoryBean;

import com.google.code.yanf4j.config.Configuration;

/**
 * Implement spring's factory bean,for integrating to spring framework.
 * 
 * @author dennis
 * 
 */
public class XMemcachedClientFactoryBean implements FactoryBean {

	private MemcachedSessionLocator sessionLocator = new ArrayMemcachedSessionLocator();
	private BufferAllocator bufferAllocator = new SimpleBufferAllocator();
	private String servers;
	private List<Integer> weights;
	@SuppressWarnings("unchecked")
	private Transcoder transcoder = new SerializingTranscoder();
	private Configuration configuration = XMemcachedClientBuilder
			.getDefaultConfiguration();
	private CommandFactory commandFactory = new TextCommandFactory();

	private Map<InetSocketAddress, AuthInfo> authInfoMap = new HashMap<InetSocketAddress, AuthInfo>();

	private String name;

	private int connectionPoolSize = MemcachedClient.DEFAULT_CONNECTION_POOL_SIZE;
	
	//added by nada on 20120422
	private long opTimeout = MemcachedClient.DEFAULT_OP_TIMEOUT;
	//added by allen on 20160428
	private boolean addShutdownHook = true;

	public final CommandFactory getCommandFactory() {
		return commandFactory;
	}

	public final void setCommandFactory(CommandFactory commandFactory) {
		this.commandFactory = commandFactory;
	}

	public XMemcachedClientFactoryBean() {

	}

	public Map<InetSocketAddress, AuthInfo> getAuthInfoMap() {
		return authInfoMap;
	}

	public void setAuthInfoMap(Map<InetSocketAddress, AuthInfo> authInfoMap) {
		this.authInfoMap = authInfoMap;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getConnectionPoolSize() {
		return connectionPoolSize;
	}

	public final void setConnectionPoolSize(int poolSize) {
		connectionPoolSize = poolSize;
	}

	public void setSessionLocator(MemcachedSessionLocator sessionLocator) {
		this.sessionLocator = sessionLocator;
	}

	public void setBufferAllocator(BufferAllocator bufferAllocator) {
		this.bufferAllocator = bufferAllocator;
	}

	@SuppressWarnings("unchecked")
	public void setTranscoder(Transcoder transcoder) {
		this.transcoder = transcoder;
	}

	public void setConfiguration(Configuration configuration) {
		this.configuration = configuration;
	}

	public String getServers() {
		return servers;
	}

	public void setServers(String servers) {
		this.servers = servers;
	}

	public MemcachedSessionLocator getSessionLocator() {
		return sessionLocator;
	}

	public BufferAllocator getBufferAllocator() {
		return bufferAllocator;
	}

	@SuppressWarnings("unchecked")
	public Transcoder getTranscoder() {
		return transcoder;
	}

	public List<Integer> getWeights() {
		return weights;
	}

	public void setWeights(List<Integer> weights) {
		this.weights = weights;
	}

	public Configuration getConfiguration() {
		return configuration;
	}

	public Object getObject() throws Exception {
		checkAttribute();
		List<InetSocketAddress> serverList = getServerList();
		int[] weightsArray = getWeightsArray(serverList);
		MemcachedClientBuilder builder = newBuilder(serverList, weightsArray);
		configBuilder(builder);
		return builder.build();
	}

	private MemcachedClientBuilder newBuilder(
			List<InetSocketAddress> serverList, int[] weightsArray) {
		MemcachedClientBuilder builder;
		if (weightsArray == null) {
			builder = new XMemcachedClientBuilder(serverList);
		} else {
			builder = new XMemcachedClientBuilder(serverList, weightsArray);
		}
		return builder;
	}

	private void configBuilder(MemcachedClientBuilder builder) {
		builder.setConfiguration(configuration);
		builder.setBufferAllocator(bufferAllocator);
		builder.setSessionLocator(sessionLocator);
		builder.setTranscoder(transcoder);
		builder.setCommandFactory(commandFactory);
		builder.setConnectionPoolSize(connectionPoolSize);
		builder.setAuthInfoMap(authInfoMap);
		builder.setName(name);
		builder.setOpTimeout(opTimeout);
		builder.setAddShutdownHook(addShutdownHook);
	}

	private int[] getWeightsArray(List<InetSocketAddress> serverList) {
		int[] weightsArray = null;
		if (serverList != null && serverList.size() > 0 && weights != null) {
			if (weights.size() < serverList.size()) {
				throw new IllegalArgumentException(
						"Weight list's size is less than server list's size");
			}
			weightsArray = new int[weights.size()];
			for (int i = 0; i < weightsArray.length; i++) {
				weightsArray[i] = weights.get(i);
			}
		}
		return weightsArray;
	}

	private List<InetSocketAddress> getServerList() {
		List<InetSocketAddress> serverList = null;

		if (servers != null && servers.length() > 0) {
			serverList = AddrUtil.getAddresses(servers);

		}
		return serverList;
	}

	private void checkAttribute() {
		if (bufferAllocator == null) {
			throw new NullPointerException("Null BufferAllocator");
		}
		if (sessionLocator == null) {
			throw new NullPointerException("Null MemcachedSessionLocator");
		}
		if (configuration == null) {
			throw new NullPointerException("Null networking configuration");
		}
		if (commandFactory == null) {
			throw new NullPointerException("Null command factory");
		}
		if (weights != null && servers == null) {
			throw new NullPointerException("Empty server list");
		}
	}

	@SuppressWarnings("unchecked")
	public Class getObjectType() {
		return MemcachedClient.class;
	}

	public boolean isSingleton() {
		return true;
	}

	public long getOpTimeout() {
		return opTimeout;
	}

	public void setOpTimeout(long opTimeout) {
		this.opTimeout = opTimeout;
    }

    public boolean isAddShutdownHook() {
        return addShutdownHook;
    }

    public void setAddShutdownHook(boolean addShutdownHook) {
        this.addShutdownHook = addShutdownHook;
    }
}
