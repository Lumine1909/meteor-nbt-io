package io.github.lumine1909.nbtio;

import com.mojang.logging.LogUtils;
import meteordevelopment.meteorclient.addons.GithubRepo;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import org.slf4j.Logger;

public class NbtIoAddon extends MeteorAddon {

    public static final Logger LOG = LogUtils.getLogger();

    @Override
    public void onInitialize() {
        LOG.info("Initializing Meteor NBT-IO Addon");
    }

    @Override
    public String getPackage() {
        return "io.github.lumine1909.nbtio";
    }

    @Override
    public GithubRepo getRepo() {
        return new GithubRepo("Lumine1909", "meteor-nbt-io");
    }
}
