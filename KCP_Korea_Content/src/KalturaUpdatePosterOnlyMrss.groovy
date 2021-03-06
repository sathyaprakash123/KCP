import tv.nativ.mio.api.plugin.command.PluginCommand

class KalturaUpdatePosterOnlyMrss extends PluginCommand

{
    // Created by Sathya, Apr, 2018. Update Kaltura Poster for Season.
    // Using Hard-coded Id for now for testing.
    static final DistributionProfileId = "1712581" // Using fixed value as per spec
    static Document document; // For creating mrss XML
    static def Season_UDO_Metadata, season_UDO, poster_asset_1609, poster_asset_0203, poster_asset_1609_id, poster_asset_0203_id
    // For storing Poster files, UDO Metadata
    static String NameCache = ""  // For processing Cast Names
    static String separator = ";" // For separating Cast details
    static List<String> TempTags  // For storing Muti-option tag values
    static String TempFilePath = "/flex/flex-enterprise/storage/media/"
    //location of temp folder where mrss is pushed
    static String DestinationFilePath = "/flex/flex-enterprise/storage/media/tmp/"  //location of file after import
    static String FileExtension = "_Update_MRSS.xml" //Extension name for mrss update file
    static String mrss_filename  //name of the mrss add kaltura publish
    static String mrss_xml_string

    public static Element CreateElement(Element ParentName, String NewElement, String TagValue) {
        Element element = document.createElement(NewElement);
        if (TagValue == null) {

            ParentName.appendChild(element);
            return element;

        } else {
            element.appendChild(document.createTextNode(TagValue));
            ParentName.appendChild(element);
            return element;
        }

    }

    public static void SetAttribute(Element ElementName, String AttributeKey, String AttributeValue)

    {

        ElementName.setAttribute(AttributeKey, AttributeValue);

    }

    public static void Obtain_Thumbnail_Images() {

    }

    public static void DisplayWriteXML(String TempFilePathValue) throws TransformerException {
        DOMSource domsource = new DOMSource(document);
        Transformer transformer = TransformerFactory.newInstance().newTransformer()
        transformer.setOutputProperty("indent", "yes")
        File file = new File(TempFilePathValue)

        if (file.exists())

        {
            file.delete()
        }


        StreamResult WriteToTemp = new StreamResult(new File(TempFilePathValue))
        transformer.transform(domsource, WriteToTemp)
        StringWriter mrssString = new StringWriter()
        transformer.transform(domsource, new StreamResult(mrssString))
        mrss_xml_string = mrssString.toString()

    }

    public static String FormatNameValues(String GenreValues, String FileNameProcess) {

        String GenreValueList
        if (FileNameProcess == null) {
            GenreValueList = GenreValues.replace("[", "");
            GenreValueList = GenreValueList.replace("]", "");
            GenreValueList = GenreValueList.replace(",", ";")
            GenreValueList = GenreValueList.replace(" ;", ";")
            GenreValueList = GenreValueList.replace("; ", ";")
            if (GenreValueList.substring(GenreValueList.length() - 1) == ";") {
                GenreValueList = GenreValueList.substring(0, GenreValueList.length() - 1)
            }
        } else {
            GenreValueList = GenreValues.replace(" ", "_")
        }

        return GenreValueList
    }


    public static void ProcessSeparateTags(String TagValues) {

        def counter = 0
        TempTags = new ArrayList<String>()

        for (int i = 0; i < TagValues.length(); i++) {
            if (TagValues.charAt(i) == ";") {
                counter++
            }

        }

        for (int j = 0; j < counter; j++) {

            TempTags.add(TagValues.substring(0, TagValues.indexOf(";")))
            TagValues = TagValues.substring(TagValues.indexOf(";") + 1, TagValues.length())
        }

        TempTags.add(TagValues)

    }



    def execute()

    {
        def AssetService = services.assetService
        def UDOService = services.userDefinedObjectService
        def objectId = context.getMioObjectVariable("triggerObject")
        def SeasonUDOId = objectId.id
        // def SeasonUDOId = context.getMioObjectVariable("triggerObject") Apply once object id is received through trigger.
        // def season_UDO = UDOService.getUserDefinedObject("Seasons", SeasonUDOId)
        season_UDO = UDOService.getUserDefinedObject("Seasons", SeasonUDOId)
        Season_UDO_Metadata = UDOService.getObjectData("Seasons", season_UDO.id)
        context.logInfo("Name of Seasons UDO is : " + season_UDO.name)
        context.logInfo("Total no of children : " + season_UDO.children.size())
        // Method to obtain names of 16:9 and 2:3 Poster names
        season_UDO.children.each { child ->



            if (child.variant != null) {
                Asset ChildAsset = AssetService.getAsset(child.id)
                def childMetadata = AssetService.getAssetMetadata(child.id)

                def jsonData = new JsonSlurper().parseText(childMetadata.toString())




                if (jsonData.ratio == "1609") {

                    poster_asset_1609 = ChildAsset.fileInformation.currentFileName
                    poster_asset_1609_id = childMetadata.getField("ovp-asset-id").value.toString()
                    context.logInfo("Child Added : " + child.displayName + "  " + child.name + "  " + "Child Variant " + child.variant.name)
                    context.setMioObjectVariable("Poster_File_1606", ChildAsset.id)

                }
                if (jsonData.ratio == "0203" != null) {

                    poster_asset_0203 = ChildAsset.fileInformation.currentFileName
                    poster_asset_0203_id = childMetadata.getField("ovp-asset-id").value.toString()
                    context.logInfo("Child Added : " + child.displayName + "  " + child.name + "  " + "Child Variant " + child.variant.name)
                    context.setMioObjectVariable("Poster_File_0203", ChildAsset.id)
                }

            }

        }

        try {

            // Declaring DOM Parser and Root element for the XML
            DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance()
            DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder()
            document = documentBuilder.newDocument()
            Element mrss = document.createElement("mrss")
            document.appendChild(mrss);
            SetAttribute(mrss, "xmlns:xsd", "http://www.w3.org/2001/XMLSchema");
            SetAttribute(mrss, "xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
            SetAttribute(mrss, "xsi:noNamespaceSchemaLocation", "ingestion.xsd");
            Element channel = CreateElement(mrss, "channel", null);
            Element item = CreateElement(channel, "item", null);
            Element action = CreateElement(item, "action", "update");
            Element type = CreateElement(item, "type", "1");
            Element referenceId = CreateElement(item, "referenceId", Season_UDO_Metadata.getField("reference-id").value)
            Element thumbnails = CreateElement(item, "thumbnails", null);
            Element thumbnail = CreateElement(thumbnails, "thumbnail", null);
            SetAttribute(thumbnail, "isDefault", "false");
            SetAttribute(thumbnail, "thumbAssetId", poster_asset_1609_id);
            Element tags1 = CreateElement(thumbnail, "tags", null);
            Element tag1 = CreateElement(tags1, "tag", "16:9");
            Element dropFolderFileContentResource = CreateElement(thumbnail, "dropFolderFileContentResource", "");
            SetAttribute(dropFolderFileContentResource, "filePath", poster_asset_1609);
            Element thumbnail1 = CreateElement(thumbnails, "thumbnail", null);
            SetAttribute(thumbnail1, "isDefault", "true");
            SetAttribute(thumbnail1, "thumbAssetId", poster_asset_0203_id);

            Element tags2 = CreateElement(thumbnail1, "tags", null);
            Element tag2 = CreateElement(tags2, "tag", "2:3");
            Element dropFolderFileContentResource1 = CreateElement(thumbnail1, "dropFolderFileContentResource", "");
            SetAttribute(dropFolderFileContentResource1, "filePath", poster_asset_0203);
            // Add filepath value attribute here
            context.logInfo("All details parsed into MRSS. Naming the xml feed now")
            mrss_filename = FormatNameValues(season_UDO.name.toString() + FileExtension, "remove spaces")
            context.logInfo("Display TempFilePath Value : "+ TempFilePath)
            DisplayWriteXML(TempFilePath + mrss_filename)
            context.logInfo("Display TempFilePath after DisplayWriteXML Method : "+ TempFilePath + mrss_filename)
            context.logInfo("Mrss Filename : " + mrss_filename + " And File Stored Path is : " + TempFilePath)
            context.setStringVariable("kaltura_mrss_temp_file_path", TempFilePath + mrss_filename)
            context.setStringVariable("kaltura_mrss_source_file_name", mrss_filename)
            context.setStringVariable("kaltura_mrss_dest_name", mrss_filename)
            context.setMioObjectVariable("SeasonUDOObject", season_UDO)
            context.setStringVariable("SeasonUDOObjectId", SeasonUDOId.toString())
            context.setStringVariable("mrss_xml_string", mrss_xml_string)
            context.setStringVariable("destination_file_path", DestinationFilePath)
            context.setStringVariable("mrss_metadata_file_name", FormatNameValues(Season_UDO_Metadata.getField("reference-id").value+ FileExtension, "remove spaces"))
            context.setStringVariable("update_type", "poster")
            context.logInfo("Context variables created for mrss file name and mrss file path")
        }

        catch (ParserConfigurationException pce) {
            pce.printStackTrace()
        }

    }

}