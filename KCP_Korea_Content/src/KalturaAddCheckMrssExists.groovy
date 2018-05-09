import tv.nativ.mio.api.plugin.command.PluginCommand

class KalturaAddCheckMrssExists extends PluginCommand
{
    def execute()
    {
        def metadataDefinitionId = "20109"
        def AssetService = services.assetService
        AssetAPIQuery assetQuery = new AssetAPIQuery()
        assetQuery.setMetadata(["new-string-1:" + context.getStringVariable("mrss_metadata_file_name")])
        assetQuery.setVariant(["MRSS"])
        assetQuery.setMetadataDefinitionId(metadataDefinitionId)
        AssetList assetList = AssetService.getAssets(assetQuery)

        if (!assetList.assets) {

            context.logInfo("No asset with the same name found in system. Will create mrss")
            return "ok"

        }

        else if (assetList.assets) {

            throw new Exception("Error - The Season has already been published to KOVP , Did you mean to update ?")
            return "error"
        }


    }

}