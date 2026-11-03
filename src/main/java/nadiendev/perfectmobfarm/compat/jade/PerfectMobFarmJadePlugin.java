package nadiendev.perfectmobfarm.compat.jade;

import nadiendev.perfectmobfarm.block.MobFarmBlock;
import nadiendev.perfectmobfarm.block.entity.MobFarmBlockEntity;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

/**
 * Jade integration. Optional: the class is only loaded by Jade itself, so the mod works without it.
 * Every reference to the Jade API must stay inside this package.
 */
@WailaPlugin
public class PerfectMobFarmJadePlugin implements IWailaPlugin {

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(MobFarmDataProvider.INSTANCE, MobFarmBlockEntity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        // The config option (id = the provider uid) is created by Jade when the provider is registered
        registration.registerBlockComponent(MobFarmComponentProvider.INSTANCE, MobFarmBlock.class);
    }
}
