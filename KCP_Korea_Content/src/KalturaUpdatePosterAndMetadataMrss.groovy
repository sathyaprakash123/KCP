import tv.nativ.mio.api.plugin.command.PluginCommand

class KalturaUpdatePosterAndMetadataMrss extends PluginCommand
{
    // Created by Sathya, Apr, 2018. Extract Seasons UDO and Generate Kaltura Series Publish XML.
    // Using Hard-coded Id for now for testing.

    static final DistributionProfileId = "1712581" // Using fixed value as per spec
    static Document document; // For creating mrss XML
    static def Season_UDO_Metadata, season_UDO, assetratio1609, assetratio0203, poster_asset_0203_id, poster_asset_1609_id // For storing Poster files, UDO Metadata
    static String NameCache = ""  // For processing Cast Names
    static String separator = ";" // For separating Cast details
    static List<String> TempTags  // For storing Muti-option tag values
    static List<String> OnAirDays
    static String TempFilePath = "/flex/flex-enterprise/storage/media/"
    //location of temp folder where mrss is pushed
    static String DestinationFilePath = "/flex/flex-enterprise/storage/media/tmp/"  //location of file after import
    static String FileExtension = "_Update_MRSS.xml" //Extension name for mrss file
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

    public static String GetKeyValue(String JsonValue, String CastDetail) {

        if (CastDetail == "cast") {
            def jsonValue = new JsonSlurper().parseText(JsonValue)
            return (jsonValue.cast[0].name)
        }
        if (CastDetail == "director") {
            def jsonValue = new JsonSlurper().parseText(JsonValue)
            return (jsonValue.director[0].name)
        }

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

    public static String ConvertOnAirDays(String OnAirDaysString) // Method to convert on week day names to binary values
    {
        OnAirDays = new ArrayList<String>()
        List OnAirDaysBinary  = new LinkedList<String>()
        String FinalBinary = ""

        // Adding Default 0 for all days
        OnAirDaysBinary.add("0")
        OnAirDaysBinary.add("0")
        OnAirDaysBinary.add("0")
        OnAirDaysBinary.add("0")
        OnAirDaysBinary.add("0")
        OnAirDaysBinary.add("0")
        OnAirDaysBinary.add("0")

        def counter = 0
        for (int i = 0; i < OnAirDaysString.length(); i++) {
            if (OnAirDaysString.charAt(i) == ";") {
                counter++
            }

        }
        for (int j = 0; j < counter; j++) {

            OnAirDays.add(OnAirDaysString.substring(0, OnAirDaysString.indexOf(";")))
            OnAirDaysString = OnAirDaysString.substring(OnAirDaysString.indexOf(";") + 1, OnAirDaysString.length())
        }
        OnAirDays.add(OnAirDaysString)

        for(String day : OnAirDays)
        {
            if (day == "MON") OnAirDaysBinary.set(0, "1")
            else if (day == "TUE") OnAirDaysBinary.set(1, "1")
            else if (day == "WED") OnAirDaysBinary.set(2, "1")
            else if (day == "THU") OnAirDaysBinary.set(3, "1")
            else if (day == "FRI") OnAirDaysBinary.set(4, "1")
            else if (day == "SAT") OnAirDaysBinary.set(5, "1")
            else if (day == "SUN") OnAirDaysBinary.set(6, "1")

        }

        for (String day : OnAirDaysBinary)
        {
            FinalBinary = FinalBinary + day
        }

        return FinalBinary
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
                def childMetadata = AssetService.getAssetMetadata(child.id)
                def jsonData = new JsonSlurper().parseText(childMetadata.toString())
                Asset ChildAsset = AssetService.getAsset(child.id)

                if (jsonData.ratio == "1609") {

                    assetratio1609 = ChildAsset.fileInformation.currentFileName
                    context.logInfo("Child Added : " + child.displayName + "  " + child.name + "  " + "Child Variant " + child.variant.name)
                    context.setMioObjectVariable("Poster_File_1606", ChildAsset.id)
                    poster_asset_1609_id = childMetadata.getField("ovp-asset-id").value.toString()
                    context.setStringVariable("poster_filename1_1609", assetratio1609)
                }
                if (jsonData.ratio == "0203") {
                    assetratio0203 = ChildAsset.fileInformation.currentFileName
                    context.logInfo("Child Added : " + child.displayName + "  " + child.name + "  " + "Child Variant " + child.variant.name)
                    context.setMioObjectVariable("Poster_File_0203", ChildAsset.id)
                    poster_asset_0203_id = childMetadata.getField("ovp-asset-id").value.toString()
                    context.setStringVariable("poster_filename2_0203", assetratio0203)
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
            Element referenceId = CreateElement(item, "referenceId", Season_UDO_Metadata.getField("reference-id").value);

            def taPartnerObject = Season_UDO_Metadata.getField("ta-partner").getValue()
            def taPartner = taPartnerObject.name
            Element userId = CreateElement(item, "userId", taPartner)

            Element name = CreateElement(item, "name", season_UDO.name.toString());
            Element description = CreateElement(item, "description", Season_UDO_Metadata.getField("description").value);
            Element tags = CreateElement(item, "tags", null);
            def tagList = Season_UDO_Metadata.getFields("tags")

            tagList.each { EachTag ->

                context.logInfo("Tag value: " + EachTag.value.toString())
                ProcessSeparateTags(FormatNameValues(EachTag.value.toString(), null))
                TempTags.each { temp ->

                    context.logInfo(temp.toString())
                    Element tag = CreateElement(tags, "tag", temp.toString());

                }


            }


            Element thumbnails = CreateElement(item, "thumbnails", null);
            Element thumbnail = CreateElement(thumbnails, "thumbnail", null);
            SetAttribute(thumbnail, "isDefault", "false");
            SetAttribute(thumbnail, "thumbAssetId", poster_asset_1609_id);




            Element tags1 = CreateElement(thumbnail, "tags", null);
            Element tag1 = CreateElement(tags1, "tag", "16:9");
            Element dropFolderFileContentResource = CreateElement(thumbnail, "dropFolderFileContentResource", "");
            SetAttribute(dropFolderFileContentResource, "filePath", assetratio1609);
            Element thumbnail1 = CreateElement(thumbnails, "thumbnail", null);
            SetAttribute(thumbnail1, "isDefault", "true");
            SetAttribute(thumbnail1, "thumbAssetId", poster_asset_0203_id);



            Element tags2 = CreateElement(thumbnail1, "tags", null);
            Element tag2 = CreateElement(tags2, "tag", "2:3");
            Element dropFolderFileContentResource1 = CreateElement(thumbnail1, "dropFolderFileContentResource", "");
            SetAttribute(dropFolderFileContentResource1, "filePath", assetratio0203);
            Element customDataItems = CreateElement(item, "customDataItems", null);
            Element customData = CreateElement(customDataItems, "customData", null);
            SetAttribute(customData, "metadataProfile", "KCPvirtual");
            Element xmlData = CreateElement(customData, "xmlData", null);
            Element metadata = CreateElement(xmlData, "metadata", null);
            Element MediaType = CreateElement(metadata, "MediaType", "Series")

            Season_UDO_Metadata.getFields("genres").each
                    { genre ->
                        def GenreValueList = FormatNameValues(genre.value.toString(), null)
                        Element OTTTAGGenre = CreateElement(metadata, "OTTTAGGenre", GenreValueList)
                    }

            Element OTTTAGCategory = CreateElement(metadata, "OTTTAGCategory", Season_UDO_Metadata.getField("category").value.toString());
            Element NUMYear = CreateElement(metadata, "NUMYear", Season_UDO_Metadata.getField("on-air-year").value.toString());
            Element OTTTAGParental_Rating_Info = CreateElement(metadata, "OTTTAGParental_Rating_Info", Season_UDO_Metadata.getField("rating-info").value.toString());

            Season_UDO_Metadata.getFields("cast").each
                    { cast ->
                        context.logInfo(cast.toJson().toString())
                        NameCache = NameCache + GetKeyValue(cast.toJson().toString(), "cast") + separator
                    }
            Element OTTTAGMain_Cast = CreateElement(metadata, "OTTTAGMain_Cast", FormatNameValues(NameCache, null));
            NameCache = ""

            Season_UDO_Metadata.getFields("director").each
                    { director ->
                        context.logInfo(director.toJson().toString())
                        NameCache = NameCache + GetKeyValue(director.toJson().toString(), "director") + separator
                    }
            Element OTTTAGDirector = CreateElement(metadata, "OTTTAGDirector", FormatNameValues(NameCache, null));
            NameCache = ""
            Element OTTTAGCountry = CreateElement(metadata, "OTTTAGCountry", Season_UDO_Metadata.getField("country").value.toString());

            def providers = [K01:'KBS', K02:'KBS2', M01:'MBC', S01:'SBS', M11:'MBC Plus', Y01:'YG ENT']
            def provider = Season_UDO_Metadata.getField("provider").getValue().toString()
            def provider_display_value = providers.get(provider)
            context.logInfo("PROVIDER DISPLAY VALUE: " + provider_display_value)
            Element OTTTAGProvider = CreateElement(metadata, "OTTTAGProvider", provider_display_value)


            Element STRINGSeriesId = CreateElement(metadata, "STRINGSeriesId", Season_UDO_Metadata.getField("reference-id").value.toString());
            Element OTTTAGSeriesAirDays = CreateElement(metadata, "OTTTAGSeriesAirDays", ConvertOnAirDays(FormatNameValues(Season_UDO_Metadata.getField("on-air-days").value.toString(), null)))
            Element STRINGAssetIsActive = CreateElement(metadata, "STRINGAssetIsActive", "true");
            Element NUMNum_Of_Episodes = CreateElement(metadata, "NUMNum_Of_Episodes", Season_UDO_Metadata.getField("num-of-episode").value.toString());
            // Add filepath value attribute here
            context.logInfo("All details parsed into MRSS. Naming the xml feed now")
            //mrss_filename = FormatNameValues(season_UDO.name.toString() + "_"+Season_UDO_Metadata.getField("reference-id").value+ FileExtension, "remove spaces")
            mrss_filename = FormatNameValues(season_UDO.name.toString() + FileExtension, "remove spaces")
            context.logInfo("Display TempFilePath Value : "+ TempFilePath)
            DisplayWriteXML(TempFilePath + mrss_filename)
            context.logInfo("Display TempFilePath after DisplayWriteXML Method : "+ TempFilePath + mrss_filename)
            context.logInfo("Mrss Filename : " + mrss_filename + " And File Stored Path is : " + TempFilePath)
            context.setStringVariable("kaltura_mrss_temp_file_path", TempFilePath + mrss_filename)
            context.setStringVariable("kaltura_mrss_source_file_name",  mrss_filename)
            context.setStringVariable("kaltura_mrss_dest_name", mrss_filename)
            context.setMioObjectVariable("SeasonUDOObject", season_UDO)
            context.setStringVariable("SeasonUDOObjectId", SeasonUDOId.toString())
            context.setStringVariable("mrss_xml_string", mrss_xml_string)
            context.setStringVariable("destination_file_path", DestinationFilePath)
            context.setStringVariable("reference_id", Season_UDO_Metadata.getField("reference-id").value.toString())
            context.setStringVariable("mrss_metadata_file_name", FormatNameValues(Season_UDO_Metadata.getField("reference-id").value+ FileExtension, "remove spaces") )
            context.logInfo("Context variables created for mrss file name and mrss file path")
            context.setStringVariable("update_type", "poster and metadata")
        }

        catch (ParserConfigurationException pce) {
            pce.printStackTrace()
        }

    }
}
