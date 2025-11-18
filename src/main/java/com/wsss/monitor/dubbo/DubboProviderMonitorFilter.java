package com.wsss.monitor.dubbo;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.RpcContext;

@Activate(group = {"provider"})
public class DubboProviderMonitorFilter extends DubboRpcMonitorFilter {

    @Override
    protected URL getUrl() {
        return RpcContext.getContext().getUrl();
    }

    @Override
    protected boolean isProviderSide() {
        return true;
    }

}
