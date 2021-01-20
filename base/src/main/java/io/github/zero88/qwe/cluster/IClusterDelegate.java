package io.github.zero88.qwe.cluster;

import java.util.List;

import io.github.zero88.qwe.exceptions.NotFoundException;
import io.github.zero88.qwe.CarlConfig.SystemConfig.ClusterConfig;
import io.vertx.core.spi.cluster.ClusterManager;

import lombok.NonNull;

public interface IClusterDelegate {

    @NonNull ClusterType getTypeName();

    @NonNull ClusterManager initClusterManager(ClusterConfig clusterConfig);

    /**
     * Find node in cluster.
     *
     * @param id node Id
     * @return cluster node
     * @throws ClusterException  if cluster manager was not initialized
     * @throws NotFoundException if cluster does not have node with given id
     */
    ClusterNode lookupNodeById(String id);

    List<ClusterNode> getAllNodes();

}