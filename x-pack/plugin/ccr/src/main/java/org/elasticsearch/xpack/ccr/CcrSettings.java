/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.ccr;

import org.elasticsearch.common.settings.ClusterSettings;
import org.elasticsearch.common.settings.Setting;
import org.elasticsearch.common.settings.Setting.Property;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.util.CombinedRateLimiter;
import org.elasticsearch.xpack.core.XPackSettings;

import java.util.Arrays;
import java.util.List;

/**
 * Container class for CCR settings.
 */
public final class CcrSettings {

    /**
     * Index setting for a following index.
     */
    public static final Setting<Boolean> CCR_FOLLOWING_INDEX_SETTING =
            Setting.boolSetting("index.xpack.ccr.following_index", false, Property.IndexScope, Property.InternalIndex);

    /**
     * Dynamic node setting for specifying the wait_for_timeout that the auto follow coordinator should be using.
     */
    public static final Setting<TimeValue> CCR_AUTO_FOLLOW_WAIT_FOR_METADATA_TIMEOUT = Setting.timeSetting(
        "ccr.auto_follow.wait_for_metadata_timeout", TimeValue.timeValueSeconds(60), Property.NodeScope, Property.Dynamic);


    /**
     * Max bytes a node can recover per second.
     */
    public static final Setting<ByteSizeValue> RECOVERY_MAX_BYTES_PER_SECOND =
        Setting.byteSizeSetting("ccr.indices.recovery.max_bytes_per_sec", new ByteSizeValue(40, ByteSizeUnit.MB),
            Setting.Property.Dynamic, Setting.Property.NodeScope);

    /**
     * The settings defined by CCR.
     *
     * @return the settings
     */
    static List<Setting<?>> getSettings() {
        return Arrays.asList(
                XPackSettings.CCR_ENABLED_SETTING,
                CCR_FOLLOWING_INDEX_SETTING,
                RECOVERY_MAX_BYTES_PER_SECOND,
                CCR_AUTO_FOLLOW_WAIT_FOR_METADATA_TIMEOUT);
    }

    private final CombinedRateLimiter ccrRateLimiter;

    public CcrSettings(Settings settings, ClusterSettings clusterSettings) {
        this.ccrRateLimiter = new CombinedRateLimiter(RECOVERY_MAX_BYTES_PER_SECOND.get(settings));
        clusterSettings.addSettingsUpdateConsumer(RECOVERY_MAX_BYTES_PER_SECOND, this::setMaxBytesPerSec);
    }

    private void setMaxBytesPerSec(ByteSizeValue maxBytesPerSec) {
        ccrRateLimiter.setMBPerSec(maxBytesPerSec);
    }

    public CombinedRateLimiter getRateLimiter() {
        return ccrRateLimiter;
    }

}
