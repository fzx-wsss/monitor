package com.wsss.monitor.dubbo;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.RpcContext;

@Activate(group = {"consumer"})
public class DubboConsumerMonitorFilter extends DubboRpcMonitorFilter {

    @Override
    protected URL getUrl() {
        return RpcContext.getContext().getConsumerUrl();
    }

    @Override
    protected boolean isProviderSide() {
        return false;
    }

}
