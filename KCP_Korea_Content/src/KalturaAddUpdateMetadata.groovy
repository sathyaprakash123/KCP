import tv.nativ.mio.api.plugin.command.PluginCommand

class KalturaAddUpdateMetadata extends PluginCommand
{
    def execute() {

        def AssetService = services.assetService
        def UDOService = services.userDefinedObjectService
        def asset = context.asset
        def asset_metadata = AssetService.getAssetMetadata(asset.id)
        asset_metadata.getField("new-string-1").setValue(context.getStringVariable("mrss_metadata_file_name").toString())
        asset_metadata.getField("type").setValue("Add")
        asset_metadata.getField("creation-date").setValue(new Date())
        AssetService.setAssetMetadata(asset.id, asset_metadata)
        context.logInfo("Asset in Context is : " + context.asset.name)
        context.logInfo("Asset storage location is : "+ context.asset.fileInformation.currentFileName)
        context.setMioObjectVariable("mrss_xml_asset", asset)
    }
}