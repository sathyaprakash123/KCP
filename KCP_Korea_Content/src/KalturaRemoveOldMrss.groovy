import tv.nativ.mio.api.plugin.command.PluginCommand

class KalturaRemoveOldMrss extends PluginCommand
{
    def execute()
    {

        def AssetId = context.getStringVariable("AssetId")
        def old_asset = services.assetService.getAsset(AssetId as long)
        context.asset = old_asset.id

    }

}