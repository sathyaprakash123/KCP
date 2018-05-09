import tv.nativ.mio.api.plugin.command.PluginCommand

class KalturaUpdateDoesMrssExist extends PluginCommand
{
    def execute() {


        def WorkflowIdToProcessEachChild = 55429
        def metadataDefinitionId = "20109"
        def AssetService = services.assetService
        AssetAPIQuery assetQuery = new AssetAPIQuery()
        assetQuery.setMetadata(["new-string-1:" + context.getStringVariable("mrss_metadata_file_name")])
        assetQuery.setVariant(["MRSS"])
        assetQuery.setMetadataDefinitionId(metadataDefinitionId)
        AssetList assetList = AssetService.getAssets(assetQuery)

        if (assetList.assets) {
            context.logInfo("Asset already present in this Name. Size of asset list equals : " + assetList.assets.size)
        } else

        {
            context.logInfo("Asset with this name not present in the system")
        }


        if (!assetList.assets) {
            return "ok"

        } else if (assetList.assets) {

            def old_asset_id = assetList.assets.first().id
            def old_asset = AssetService.getAsset(old_asset_id as long)
            context.logInfo(old_asset.fileInformation.currentLocation)
            context.logInfo("Old Asset Name: " + assetList.assets.first().name + "Existing Asset id : " + assetList.assets.first().id)
            context.logInfo("Duplicate Assets found and were deleted. Replacing with new Mrss for Update")
            def wFlow = new NewWorkflow()
            wFlow.setDefinitionId(WorkflowIdToProcessEachChild)
            Map<String, String> wFlowVariables = new HashMap<String, String>()
            wFlowVariables.put("AssetId", old_asset.id.toString())
            wFlow.setStringVariables(wFlowVariables)
            WorkflowInstance childWorkflowInstance = services.workflowService.createWorkflow(wFlow)

            return "ok"


        } else {
            return "error"
        }

    }
}