package org.jing.decrypter.skeleton2Json;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.jing.core.lang.ExceptionHandler;
import org.jing.core.lang.JingException;
import org.jing.core.logger.JingLogger;
import org.jing.core.util.GenericUtil;
import org.jing.core.util.StringUtil;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Description: <br>
 *
 * @author: bks <br>
 * @createDate: 2020-07-09 <br>
 */
public class Arknights {
    private static final JingLogger LOGGER = JingLogger.getLogger(Arknights.class);

    private static final String FILE_SUFFIX = ".skel";

    private static final String[] TRANSFORM_MOD = new String[]{
        "normal",
        "onlyTranslation",
        "noRotationOrReflection",
        "noScale",
        "noScaleOrReflection"
    };

    private static final String[] BLEND_MODE = new String[]{
        "normal",
        "additive",
        "multiply",
        "screen",
    };

    private static final String[] POSITION_MODE = new String[]{
        "fixed",
        "percent"
    };

    private static final String[] SPACING_MODE = new String[]{
        "length",
        "fixed",
        "percent"
    };

    private static final String[] ROTATE_MODE = new String[]{
        "tangent",
        "chain",
        "chainScale"
    };
    final static private int ATTACHMENT_TYPE_REGION = 0;
    final static private int ATTACHMENT_TYPE_BOUNDINGBOX = 1;
    final static private int ATTACHMENT_TYPE_MESH = 2;
    final static private int ATTACHMENT_TYPE_LINKEDMESH = 3;
    final static private int ATTACHMENT_TYPE_PATH = 4;
    final static private int ATTACHMENT_TYPE_POINT = 5;
    final static private int ATTACHMENT_TYPE_CLIPPIN = 6;
    private static final int SLOT_ATTACHMENT = 0;
    private static final int SLOT_COLOR = 1;
    private static final int SLOT_TWO_COLOR = 2;
    private static final int CURVE_LINEAR = 0;
    private static final int CURVE_STEPPED = 1;
    private static final int CURVE_BEZIER = 2;
    private static final int BONE_ROTATE = 0;
    private static final int BONE_TRANSLATE = 1;
    private static final int BONE_SCALE = 2;
    private static final int BONE_SHEAR = 3;
    private static final int PATH_POSITION = 0;
    private static final int PATH_SPACING = 1;
    private static final int PATH_MIX = 2;

    private float scale = 1;

    private JSONObject retJson = new JSONObject();

    private InputStream inputStream = null;

    private boolean nonessential = false;

    private List<String> skinsNameList = new ArrayList<String>();

    private List<String> eventsNameList = new ArrayList<String>();

    public Arknights(String filePath) throws JingException {
        try {
            if (!filePath.endsWith(".skel") && !filePath.endsWith(".skel.txt")) {
                ExceptionHandler.publish("Invalid file suffix.");
            }
            inputStream = new FileInputStream(new File(filePath));
        }
        catch (Exception e) {
            ExceptionHandler.publish(e);
        }
    }

    public Arknights(File file) throws JingException {
        try {
            if (!file.getName().endsWith(".skel") && !file.getName().endsWith(".skel.txt")) {
                ExceptionHandler.publish("Invalid file suffix.");
            }
            inputStream = new FileInputStream(file);
        }
        catch (Exception e) {
            ExceptionHandler.publish(e);
        }
    }

    @Override
    protected void finalize() throws Throwable {
        close();
        super.finalize();
    }

    public void close() throws JingException {
        try {
            if (null != inputStream) {
                inputStream.close();
            }
        }
        catch (Exception e) {
            ExceptionHandler.publish(e);
        }
    }

    public int read() throws JingException {
        try {
            return this.inputStream.read();
        }
        catch (Exception e) {
            ExceptionHandler.publish(e);
            return -1;
        }
    }

    public int readInt(boolean optimizePositive) throws JingException {
        try {
            // int b_ = readByte();
            int b = this.read();
            // System.out.println((char) b);
            // System.out.println((char) 0);
            int result = b & 127;
            if ((b & 128) != 0) {
                b = this.read();
                result |= (b & 127) << 7;
                if ((b & 128) != 0) {
                    b = this.read();
                    result |= (b & 127) << 14;
                    if ((b & 128) != 0) {
                        b = this.read();
                        result |= (b & 127) << 21;
                        if ((b & 128) != 0) {
                            b = this.read();
                            result |= (b & 127) << 28;
                        }
                    }
                }
            }
            return optimizePositive ? result : result >>> 1 ^ -(result & 1);
        }
        catch (Exception e) {
            ExceptionHandler.publish(e);
            return -1;
        }
    }

    public int readInt() throws JingException {
        try {
            int ch1 = inputStream.read();
            int ch2 = inputStream.read();
            int ch3 = inputStream.read();
            int ch4 = inputStream.read();
            if ((ch1 | ch2 | ch3 | ch4) < 0)
                throw new EOFException();
            return ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0));
        }
        catch (Exception e) {
            ExceptionHandler.publish(e);
            return -1;
        }
    }

    public short readShort() throws JingException {
        try {
            int ch1 = inputStream.read();
            int ch2 = inputStream.read();
            if ((ch1 | ch2) < 0)
                throw new EOFException();
            return (short)((ch1 << 8) + (ch2 << 0));
        }
        catch (Exception e) {
            ExceptionHandler.publish(e);
            return -1;
        }
    }

    public float readFloat() throws JingException {
        return Float.intBitsToFloat(readInt());
    }

    public String readString() throws JingException {
        try {
            int byteCount = readInt(true);
            switch (byteCount) {
                case 0:
                    return null;
                case 1:
                    return "";
            }
            byteCount--;
            char[] chars = new char[byteCount];
            int charCount = 0;
            for (int i = 0; i < byteCount; ) {
                int b = read();
                switch (b >> 4) {
                    case -1:
                        throw new EOFException();
                    case 12:
                    case 13:
                        chars[charCount++] = (char) ((b & 0x1F) << 6 | read() & 0x3F);
                        i += 2;
                        break;
                    case 14:
                        chars[charCount++] = (char) ((b & 0x0F) << 12 | (read() & 0x3F) << 6 | read() & 0x3F);
                        i += 3;
                        break;
                    default:
                        chars[charCount++] = (char) b;
                        i++;
                }
            }
            return new String(chars, 0, charCount);
        }
        catch (Exception e) {
            ExceptionHandler.publish(e);
            return null;
        }
    }

    public final byte readByte() throws JingException {
        try {
            int ch = read();
            if (ch < 0)
                throw new EOFException();
            return (byte)(ch);
        }
        catch (Exception e) {
            ExceptionHandler.publish(e);
            return -1;
        }
    }

    public final boolean readBoolean() throws JingException {
        try {
            int ch = inputStream.read();
            if (ch < 0)
                throw new EOFException();
            return (ch != 0);
        }
        catch (Exception e) {
            ExceptionHandler.publish(e);
            return false;
        }
    }

    private JSONObject readSkin(boolean nonessential, int index, String name) throws JingException {
        JSONObject skinJson = new JSONObject();
        JSONObject slotJson;
        int count = readInt(true);
        int slotIndex;
        for (int i$ = 0; i$ < count; i$++) {
            slotIndex = readInt(true);
            slotJson = new JSONObject();
            for (int j$ = 0, attachCount = readInt(true); j$ < attachCount; j$++) {
                String attachName = readString();
                readAttachment(slotJson, attachName, nonessential);
                setList(skinsNameList, index, name);
            }
            skinJson.element(retJson.getJSONArray("slots").getJSONObject(slotIndex).optString("name", null), slotJson);
        }
        return skinJson;
    }

    private void readAttachment(JSONObject attachments, String defaultName, boolean nonessential) throws JingException {
        String attachName =StringUtil.ifEmpty(readString(), defaultName);
        JSONObject attachJson = new JSONObject();
        int type = readByte();
        String pathName;
        int vertexCount;
        int colorInt;
        JSONArray verticesArr;
        float width;
        float height;
        switch (type) {
            case ATTACHMENT_TYPE_REGION:
                pathName = readString();
                pathName = null == pathName ? attachName : pathName;
                attachJson.element("type", "region");
                attachJson.element("name", attachName);
                attachJson.element("path", pathName.trim());
                attachJson.element("rotation", readFloat());
                attachJson.element("x", readFloat() * this.scale);
                attachJson.element("y", readFloat() * this.scale);
                attachJson.element("scaleX", readFloat());
                attachJson.element("scaleY", readFloat());
                attachJson.element("width", readFloat() * this.scale);
                attachJson.element("height", readFloat() * this.scale);
                colorInt = readInt();
                attachJson.element("color", rgba8888ToString(colorInt));
                break;
            case ATTACHMENT_TYPE_BOUNDINGBOX:
                attachJson.element("type", "boundingbox");
                attachJson.element("name", attachName);
                vertexCount = readInt(true);
                attachJson.element("vertexCount", vertexCount);
                verticesArr = new JSONArray();
                readVertices(vertexCount, verticesArr);
                attachJson.element("vertices", verticesArr);
                if (nonessential) {
                    colorInt = readInt();
                    attachJson.element("color", rgba8888ToString(colorInt));
                }
                break;
            case ATTACHMENT_TYPE_MESH:
                pathName = readString();
                pathName = null == pathName ? attachName : pathName;
                attachJson.element("type", "mesh");
                attachJson.element("name", attachName);
                attachJson.element("path", pathName);
                colorInt = readInt();
                attachJson.element("color", rgba8888ToString(colorInt));
                vertexCount = readInt(true);
                JSONArray uvsArray = new JSONArray();
                readFloatArray(vertexCount << 1, 1, uvsArray);
                attachJson.element("uvs", uvsArray);
                JSONArray trianglesArray = new JSONArray();
                readShortArray(trianglesArray);
                attachJson.element("triangles", trianglesArray);
                JSONArray verticesArray = new JSONArray();
                readVertices(vertexCount, verticesArray);
                attachJson.element("vertices", verticesArray);
                attachJson.element("hull", readInt(true) << 1);
                if (nonessential) {
                    JSONArray edgesArray = new JSONArray();
                    readShortArray(edgesArray);
                    width = readFloat() * this.scale;
                    height = readFloat() * this.scale;
                    attachJson.element("edges", edgesArray);
                    attachJson.element("width", width);
                    attachJson.element("height", height);
                }
                break;
            case ATTACHMENT_TYPE_LINKEDMESH:
                pathName = readString();
                pathName = null == pathName ? attachName : pathName;
                attachJson.element("type", "linkedmesh");
                attachJson.element("name", attachName);
                attachJson.element("path", pathName);
                colorInt = readInt();
                attachJson.element("color", rgba8888ToString(colorInt));
                attachJson.element("skin", readString());
                attachJson.element("parent", readString());
                attachJson.element("deform", readBoolean());
                if (nonessential) {
                    width = readFloat() * this.scale;
                    height = readFloat() * this.scale;
                    attachJson.element("width", width);
                    attachJson.element("height", height);
                }
                break;
            case ATTACHMENT_TYPE_PATH:
                attachJson.element("type", "path");
                attachJson.element("name", attachName);
                attachJson.element("closed", readBoolean());
                attachJson.element("constantSpeed", readBoolean());
                vertexCount = readInt(true);
                attachJson.element("vertexCount", vertexCount);
                verticesArr = new JSONArray();
                readVertices(vertexCount, verticesArr);
                attachJson.element("vertices", verticesArr);
                int length = vertexCount / 3;
                JSONArray lengthArr = new JSONArray();
                for (int i$ = 0; i$ < length; i$++) {
                    lengthArr.add(readFloat() * this.scale);
                }
                attachJson.element("lengths", lengthArr);
                if (nonessential) {
                    colorInt = readInt();
                    attachJson.element("color", rgba8888ToString(colorInt));
                }
                break;
            case ATTACHMENT_TYPE_POINT:
                attachJson.element("type", "point");
                attachJson.element("name", attachName);
                attachJson.element("rotation", readFloat());
                attachJson.element("x", readFloat() * this.scale);
                attachJson.element("y", readFloat() * this.scale);
                if (nonessential) {
                    colorInt = readInt();
                    attachJson.element("color", rgba8888ToString(colorInt));
                }
                break;
            case ATTACHMENT_TYPE_CLIPPIN:
                attachJson.element("type", "clipping");
                attachJson.element("name", attachName);
                attachJson.element("end", readInt(true));
                vertexCount = readInt(true);
                attachJson.element("vertexCount", vertexCount);
                verticesArr = new JSONArray();
                readVertices(vertexCount, verticesArr);
                attachJson.element("vertices", verticesArr);
                if (nonessential) {
                    colorInt = readInt();
                    attachJson.element("color", rgba8888ToString(colorInt));
                }
                break;
        }
        attachments.element(attachName, attachJson);
    }

    private void readVertices(int vertexCount, JSONArray verticesArr) throws JingException {
        if (!readBoolean()) {
            readFloatArray(vertexCount << 1, this.scale, verticesArr);
            return;
        }
        for (int i$ = 0; i$ < vertexCount; i$++) {
            int boneCount = readInt(true);
            verticesArr.add(boneCount);
            for (int j$ = 0; j$ < boneCount; j$++) {
                verticesArr.add(readInt(true));
                verticesArr.add(readFloat() * this.scale);
                verticesArr.add(readFloat() * this.scale);
                verticesArr.add(readFloat());
            }
        }
    }

    private void readFloatArray(int size, float scale, JSONArray floatArray) throws JingException {
        List<Float> floatList = new ArrayList<>();
        if (scale == 1) {
            for (int i$ = 0; i$ < size; i$++) {
                floatList.add(readFloat());
            }
        }
        else {
            for (int i = 0; i < size; i++) {
                floatList.add(readFloat() * scale);
            }
        }
        floatArray.addAll(floatList);
    }

    private void readShortArray(JSONArray shortArray) throws JingException {
        int arrSize = readInt(true);
        for (int i$ = 0; i$ < arrSize; i$++) {
            shortArray.add(readShort());
        }
    }

    private void readCurve(JSONObject timeline) throws JingException {
        switch (readByte()) {
            case CURVE_LINEAR:
                timeline.element("curve", "linear");
                break;
            case CURVE_STEPPED:
                timeline.element("curve", "stepped");
                break;
            case CURVE_BEZIER:
                JSONArray jsonArray = new JSONArray();
                for (int i$ = 0; i$ < 4; i$++) {
                    jsonArray.add(readFloat());
                }
                timeline.element("curve", jsonArray);
                break;
        }
    }

    private void readAnimation(String name, JSONObject animations) throws JingException {
        JSONObject animation = new JSONObject();

        // Slots timelines.
        int count = readInt(true);
        JSONObject slots = new JSONObject();
        if (0 != count) {
            JSONObject slot;
            String slotName;
            int timelineType;
            int tempCount;
            JSONArray tempJsonArr;
            JSONObject tempJson;
            for (int $i = 0; $i < count; $i++) {
                slotName = retJson.getJSONArray("slots").getJSONObject(readInt(true)).optString("name", "");
                slot = new JSONObject();
                for (int j$ = 0, slotsCount = readInt(true); j$ < slotsCount; j$++) {
                    timelineType = readByte();
                    tempCount = readInt(true);
                    switch (timelineType) {
                        case SLOT_ATTACHMENT:
                            tempJsonArr = new JSONArray();
                            for (int k$ = 0; k$ < tempCount; k$++) {
                                tempJson = new JSONObject();
                                tempJson.element("time", readFloat());
                                tempJson.element("name", readString());
                                tempJsonArr.add(tempJson);
                            }
                            slot.element("attachment", tempJsonArr);
                            break;
                        case SLOT_COLOR: {
                            tempJsonArr = new JSONArray();
                            for (int k$ = 0; k$ < tempCount; k$++) {
                                tempJson = new JSONObject();
                                float time = readFloat();
                                int colorInt = readInt();
                                tempJson.element("color", rgba8888ToString(colorInt));
                                tempJson.element("time", time);
                                if (k$ < tempCount - 1) {
                                    readCurve(tempJson);
                                }
                                tempJsonArr.add(tempJson);
                            }
                            slot.element("color", tempJsonArr);
                            break;
                        }
                        case SLOT_TWO_COLOR: {
                            tempJsonArr = new JSONArray();
                            for (int k$ = 0; k$ < tempCount; k$++) {
                                tempJson = new JSONObject();
                                float time = readFloat();
                                int light = readInt();
                                int dark = readInt();
                                tempJson.element("light", rgba8888ToString(light));
                                tempJson.element("dark", rgba8888ToString(dark));
                                tempJson.element("time", time);
                                if (k$ < tempCount - 1) {
                                    readCurve(tempJson);
                                }
                                tempJsonArr.add(tempJson);
                            }
                            slot.element("twoColor", tempJsonArr);
                            break;
                        }
                    }
                }
                slots.element(slotName, slot);
            }
        }
        animation.element("slots", slots);

        // Bone timelines.
        count = readInt(true);
        JSONObject bones = new JSONObject();
        if (0 != count) {
            JSONObject bone;
            int boneIndex;
            int bonesCount;
            int timelineType;
            int tempCount;
            JSONArray tempJsonArr;
            JSONObject tempJson;
            for (int i$ = 0; i$ < count; i$++) {
                boneIndex = readInt(true);
                bone = new JSONObject();
                bonesCount = readInt(true);
                for (int j$ = 0; j$ < bonesCount; j$++) {
                    timelineType = readByte();
                    tempCount = readInt(true);
                    if (timelineType == BONE_ROTATE) {
                        tempJsonArr = new JSONArray();
                        for (int k$ = 0; k$ < tempCount; k$++) {
                            tempJson = new JSONObject();
                            tempJson.element("time", readFloat());
                            tempJson.element("angle", readFloat());
                            if (k$ < tempCount - 1) {
                                readCurve(tempJson);
                            }
                            tempJsonArr.add(tempJson);
                        }
                        bone.element("rotate", tempJsonArr);
                    }
                    else {
                        float scale = 1;
                        if (timelineType == BONE_TRANSLATE) {
                            scale = this.scale;
                        }
                        tempJsonArr = new JSONArray();
                        for (int k$ = 0; k$ < tempCount; k$++) {
                            tempJson = new JSONObject();
                            tempJson.element("time", readFloat());
                            tempJson.element("x", readFloat() * scale);
                            tempJson.element("y", readFloat() * scale);
                            if (k$ < tempCount - 1) {
                                readCurve(tempJson);
                            }
                            tempJsonArr.add(tempJson);
                        }
                        switch (timelineType) {
                            case BONE_TRANSLATE:
                                bone.element("translate", tempJsonArr);
                                break;
                            case BONE_SCALE:
                                bone.element("scale", tempJsonArr);
                                break;
                            case BONE_SHEAR:
                                bone.element("shear", tempJsonArr);
                                break;
                        }
                    }
                }
                bones.element(retJson.getJSONArray("bones").getJSONObject(boneIndex).optString("name", ""), bone);
            }
        }
        animation.element("bones", bones);

        // IK constraint timelines.
        count = readInt(true);
        JSONObject iks = new JSONObject();
        if (0 != count) {
            int ikIndex;
            JSONArray tempJsonArr;
            JSONObject tempJson;
            for (int i$ = 0; i$ < count; i$++) {
                ikIndex = readInt(true);
                tempJsonArr = new JSONArray();
                for (int j$ = 0, iksCount = readInt(true); j$ < iksCount; j$++) {
                    tempJson = new JSONObject();
                    tempJson.element("time", readFloat());
                    tempJson.element("mix", readFloat());
                    tempJson.element("bendPositive", readByte() == 1);
                    if (j$ < iksCount - 1) {
                        readCurve(tempJson);
                    }
                    tempJsonArr.add(tempJson);
                }
                iks.element(retJson.getJSONArray("ik").getJSONObject(ikIndex).optString("name", ""), tempJsonArr);
            }
        }
        animation.element("ik", iks);

        // Transform constraint timelines.
        count = readInt(true);
        JSONObject transforms = new JSONObject();
        if (0 != count) {
            int transformIndex;
            int transformsCount;
            JSONArray tempJsonArr;
            JSONObject tempJson;
            for (int i$ = 0; i$ < count; i$++) {
                transformIndex = readInt(true);
                tempJsonArr = new JSONArray();
                transformsCount = readInt(true);
                for (int j$ = 0; j$ < transformsCount; j$++) {
                    tempJson = new JSONObject();
                    tempJson.element("time", readFloat());
                    tempJson.element("rotateMix", readFloat());
                    tempJson.element("translateMix", readFloat());
                    tempJson.element("scaleMix", readFloat());
                    tempJson.element("shearMix", readFloat());
                    if (j$ < transformsCount - 1) {
                        readCurve(tempJson);
                    }
                    tempJsonArr.add(tempJson);
                }
                transforms.element(retJson.getJSONArray("transform").getJSONObject(transformIndex).optString("name", ""), tempJsonArr);
            }
        }
        animation.element("transform", transforms);

        // Path constraint timelines.
        count = readInt(true);
        JSONObject paths = new JSONObject();
        if (0 != count) {
            JSONObject pathIncident;
            JSONObject path;
            int pathCount;
            int timelineType;
            int tempCount;
            String timeName;
            JSONArray tempJsonArr;
            JSONObject tempJson;
            // 此处原代码为 for (int i = 0, n = input.readInt(true); i < n; i++) {
            // 但是参照上下文此处应该用前面取的count了
            for (int i$ = 0; i$ < count; i$++) {
                path = new JSONObject();
                pathIncident = retJson.getJSONArray("path").getJSONObject(readInt(true));
                pathCount = readInt(true);
                for (int j$ = 0; j$ < pathCount; j$++) {
                    timelineType = readByte();
                    tempCount = readInt(true);
                    if (timelineType == PATH_POSITION || timelineType == PATH_SPACING) {
                        timeName = timelineType == PATH_POSITION ? "position" : "spacing";
                        float scale = 1;
                        if (timelineType == PATH_SPACING) {
                            if ("length".equals(pathIncident.optString("spacingMode", "")) || "fixed".equals(pathIncident.optString("spacingMode", ""))) {
                                scale = this.scale;
                            }
                        }
                        else {
                            if ("fixed".equals(pathIncident.optString("positionMode", ""))) {
                                scale = this.scale;
                            }
                        }
                        tempJsonArr = new JSONArray();
                        for (int k$ = 0; k$ < tempCount; k$++) {
                            tempJson = new JSONObject();
                            tempJson.element("time", readFloat());
                            tempJson.element(timeName, readFloat() * scale);
                            if (k$ < tempCount - 1) {
                                readCurve(tempJson);
                            }
                            tempJsonArr.add(tempJson);
                        }
                        path.element(timeName, tempJsonArr);
                    }
                    else if (timelineType == PATH_MIX) {
                        tempJsonArr = new JSONArray();
                        for (int k$ = 0; k$ < tempCount; k$++) {
                            tempJson = new JSONObject();
                            tempJson.element("time", readFloat());
                            tempJson.element("rotateMix", readFloat());
                            tempJson.element("translateMix", readFloat());
                            if (k$ < tempCount - 1) {
                                readCurve(tempJson);
                            }
                            tempJsonArr.add(tempJson);
                        }
                        path.element("mix", tempJsonArr);
                    }
                }
                paths.element(pathIncident.optString("name", ""), path);
            }
        }
        animation.element("paths", paths);

        // Deform timelines.
        count = readInt(true);
        JSONObject deforms = new JSONObject();
        if (0 != count) {
            JSONObject deform;
            JSONObject slot;
            int skinIndex;
            String skinName;
            int deformCount;
            int slotIndex;
            int slotCount;
            String timeLineName;
            float time;
            float[] deformArr;
            int start;
            int end;
            JSONArray tempJsonArr;
            JSONObject tempJson;
            JSONArray tempJsonArr$;
            for (int i$ = 0; i$ < count; i$++) {
                skinIndex = readInt(true);
                skinName = getList(skinsNameList, skinIndex);
                deform = new JSONObject();
                deformCount = readInt(true);
                for (int j$ = 0; j$ < deformCount; j$++) {
                    slotIndex = readInt(true);
                    slot = new JSONObject();
                    slotCount = readInt(true);
                    for (int k$ = 0; k$ < slotCount; k$++) {
                        timeLineName = readString();
                        tempJsonArr = new JSONArray();
                        for (int l$ = 0, tempCount = readInt(true); l$ < tempCount; l$++) {
                            tempJson = new JSONObject();
                            time = readFloat();
                            tempJson.element("time", time);
                            end = readInt(true);
                            if (end != 0) {
                                deformArr = new float[end];
                                start = readInt(true);
                                if (this.scale == 1) {
                                    for (int m$ = 0; m$ < end; m$++) {
                                        deformArr[m$] = this.readFloat();
                                    }
                                }
                                else {
                                    for (int m$ = 0; m$ < end; m$++) {
                                        deformArr[m$] = this.readFloat() * this.scale;
                                    }
                                }
                                tempJson.element("offset", start);
                                tempJsonArr$ = new JSONArray();
                                for (Object o : deformArr) {
                                    if (null == o) {
                                        tempJsonArr$.add(0f);
                                    }
                                    else {
                                        tempJsonArr$.add(o);
                                    }
                                }
                                tempJson.element("vertices", tempJsonArr$);
                            }
                            if (l$ < tempCount - 1) {
                                readCurve(tempJson);
                            }
                            tempJsonArr.add(tempJson);
                        }
                        slot.element(timeLineName, tempJsonArr);
                    }
                    deform.element(retJson.getJSONArray("slots").getJSONObject(slotIndex).optString("name", ""), slot);
                }
                deforms.element(skinName, deform);
            }
        }
        animation.element("deform", deforms);

        // Draw order timeline.
        count = readInt(true);
        if (0 != count) {
            JSONArray drawOrders = new JSONArray();
            JSONObject drawOrder;
            JSONArray offsets;
            JSONObject offset;
            float time;
            int offsetCount;
            for (int i$ = 0; i$ < count; i$++) {
                drawOrder = new JSONObject();
                time = readFloat();
                offsetCount = readInt(true);
                offsets = new JSONArray();
                for (int j$ = 0; j$ < offsetCount; j$++) {
                    offset = new JSONObject();
                    offset.element("slot", retJson.getJSONArray("slots").getJSONObject(readInt(true)).optString("name", ""));
                    offset.element("offset", readInt(true));
                    offsets.add(offset);
                }
                drawOrder.element("offsets", offsets);
                drawOrder.element("time", time);
                drawOrders.add(drawOrder);
            }
            animation.element("drawOrder", drawOrders);
        }

        // Event timeline.
        count = readInt(true);
        if (0 != count) {
            JSONArray events = new JSONArray();
            JSONObject event;
            JSONObject eventIncident;
            float time;
            String eventName;
            for (int i$ = 0; i$ < count; i$++) {
                time = readFloat();
                eventName = getList(eventsNameList, readInt(true));
                eventIncident = retJson.getJSONObject("events").getJSONObject(eventName);
                event = new JSONObject();
                event.element("name", eventName);
                event.element("int", readInt(false));
                event.element("float", readFloat());
                event.element("string", readBoolean() ? readString() : eventIncident.optString("string", ""));
                event.element("time", time);
                events.add(event);
            }
            animation.element("events", events);
        }
        animations.element(name, animation);
    }

    public void decrypter() throws JingException {
        JSONObject skeleton = createSkeleton();
        LOGGER.debug(skeleton.toString());
        retJson.element("skeleton", skeleton);

        JSONArray bonesArray = createBones();
        LOGGER.debug(bonesArray.toString());
        retJson.element("bones", bonesArray);

        JSONArray slotsArray = createSlots();
        LOGGER.debug(slotsArray.toString());
        retJson.element("slots", slotsArray);

        JSONArray iksArray = createIKs();
        LOGGER.debug(iksArray.toString());
        retJson.element("ik", iksArray);

        JSONArray transformsArray = createTransforms();
        LOGGER.debug(transformsArray.toString());
        retJson.element("transform", transformsArray);

        JSONArray pathsArray = createPaths();
        LOGGER.debug(pathsArray.toString());
        retJson.element("path", pathsArray);

        JSONObject skins = createSkins();
        LOGGER.debug(skins.toString());
        retJson.element("skins", skins);
        JSONArray jsonArray = new JSONArray();
        for (int i$ = 0, count = GenericUtil.countList(skinsNameList); i$ < count; i$++) {
            jsonArray.add(skinsNameList.get(i$));
        }
        retJson.element("skinsName", jsonArray);

        JSONObject events = createEvents();
        LOGGER.debug(events.toString());
        retJson.element("events", events);
        jsonArray = new JSONArray();
        for (int i$ = 0, count = GenericUtil.countList(eventsNameList); i$ < count; i$++) {
            jsonArray.add(eventsNameList.get(i$));
        }
        retJson.element("eventsName", jsonArray);

        JSONObject animations = createAnimations();
        LOGGER.debug(animations.toString());
        retJson.element("animations", animations);

        LOGGER.debug(retJson.toString());
    }

    private JSONObject createSkeleton() throws JingException {
        String hash = readString();
        String version = readString();
        float width = readFloat();
        float height = readFloat();
        JSONObject skeleton = new JSONObject();
        skeleton.element("hash", hash);
        skeleton.element("spine", version);
        skeleton.element("width", width);
        skeleton.element("height", height);
        this.nonessential = readBoolean();
        if (this.nonessential) {
            float fps = readFloat();
            String imagesPath = readString();
            skeleton.element("fps", fps);
            skeleton.element("images", imagesPath);
        }
        return skeleton;
    }

    private JSONArray createBones() throws JingException {
        JSONArray retArr = new JSONArray();
        int size = readInt(true);
        JSONObject bone;
        int colorInt;
        for (int i$ = 0; i$ < size; i$++) {
            bone = new JSONObject();
            bone.element("name", readString());
            bone.element("parent", (Object) null);
            if (0 != i$) {
                bone.element("parent", retArr.getJSONObject(readInt(true)).optString("name", null));
            }
            bone.element("rotation", readFloat());
            bone.element("x", readFloat() * this.scale);
            bone.element("y", readFloat() * this.scale);
            bone.element("scaleX", readFloat());
            bone.element("scaleY", readFloat());
            bone.element("shearX", readFloat());
            bone.element("shearY", readFloat());
            bone.element("length", readFloat() * this.scale);
            bone.element("transform", TRANSFORM_MOD[readInt(true)]);
            if (this.nonessential) {
                colorInt = readInt();
                bone.element("color", rgba8888ToString(colorInt));
            }
            retArr.add(bone);
        }
        return retArr;
    }

    private JSONArray createSlots() throws JingException {
        JSONArray retArr = new JSONArray();
        int size = readInt(true);
        JSONObject slot;
        int colorInt;
        for (int i$ = 0; i$ < size; i$++) {
            slot = new JSONObject();
            slot.element("name", readString());
            slot.element("bone", retJson.getJSONArray("bones").getJSONObject(readInt(true)).optString("name", null));
            colorInt = readInt();
            slot.element("color", rgba8888ToString(colorInt));
            slot.element("attachment", readString());
            slot.element("blend", BLEND_MODE[readInt(true)]);
            retArr.add(slot);
        }
        return retArr;
    }

    private JSONArray createIKs() throws JingException {
        JSONArray retArr = new JSONArray();
        int size = readInt(true);
        JSONObject ik;
        JSONArray bonesArr;
        for (int i$ = 0; i$ < size; i$++) {
            ik = new JSONObject();
            ik.element("name", readString());
            ik.element("order", readInt(true));
            bonesArr = new JSONArray();
            for (int j$ = 0, bonesSize = readInt(true); j$ < bonesSize; j$++) {
                bonesArr.add(retJson.getJSONArray("bones").getJSONObject(readInt(true)).optString("name", null));
            }
            ik.element("bones", bonesArr);
            ik.element("target", retJson.getJSONArray("bones").getJSONObject(readInt(true)).optString("name", null));
            ik.element("mix", readFloat());
            ik.element("bendPositive", 1 == readByte());
            retArr.add(ik);
        }
        return retArr;
    }

    private JSONArray createTransforms() throws JingException {
        JSONArray retArr = new JSONArray();
        int size = readInt(true);
        JSONObject transform;
        JSONArray bonesArr;
        for (int i$ = 0; i$ < size; i$++) {
            transform = new JSONObject();
            transform.element("name", readString());
            transform.element("order", readInt(true));
            bonesArr = new JSONArray();
            for (int j = 0, bonesSize = readInt(true); j < bonesSize; j++) {
                bonesArr.add(retJson.getJSONArray("bones").getJSONObject(readInt(true)).optString("name", null));
            }
            transform.element("bones", bonesArr);
            transform.element("target", retJson.getJSONArray("bones").getJSONObject(readInt(true)).optString("name", null));
            transform.element("rotation", readFloat());
            transform.element("x", readFloat() * this.scale);
            transform.element("y", readFloat() * this.scale);
            transform.element("scaleX", readFloat());
            transform.element("scaleY", readFloat());
            transform.element("shearY", readFloat());
            transform.element("rotateMix", readFloat());
            transform.element("translateMix", readFloat());
            transform.element("scaleMix", readFloat());
            transform.element("shearMix", readFloat());
            retArr.add(transform);
        }
        return retArr;
    }

    private JSONArray createPaths() throws JingException {
        JSONArray retArr = new JSONArray();
        int size = readInt(true);
        JSONObject path;
        JSONArray bonesArr;
        float position;
        float spacing;
        for (int i$ = 0; i$ < size; i$++) {
            path = new JSONObject();
            path.element("name", readString());
            path.element("order", readInt(true));
            bonesArr = new JSONArray();
            for (int j$ = 0, bonesSize = readInt(true); j$ < bonesSize; j$++) {
                bonesArr.add(retJson.getJSONArray("bones").getJSONObject(readInt(true)).optString("name", null));
            }
            path.element("bones", bonesArr);
            path.element("target", retJson.getJSONArray("slots").getJSONObject(readInt(true)).optString("name", null));
            path.element("positionMode", POSITION_MODE[readInt(true)]);
            path.element("spacingMode", SPACING_MODE[readInt(true)]);
            path.element("rotateMode", ROTATE_MODE[readInt(true)]);
            path.element("rotation", readFloat());
            position = readFloat();
            if ("fixed".equals(path.optString("positionMode"))) {
                position *= this.scale;
            }
            path.element("position", position);
            spacing = readFloat();
            if ("length".equals(path.optString("spacingMode")) || "fixed".equals(path.optString("positionMode"))) {
                spacing *= this.scale;
            }
            path.element("spacing", spacing);
            path.element("rotateMix", readFloat());
            path.element("translateMix", readFloat());
            retArr.add(path);
        }
        return retArr;
    }

    private JSONObject createSkins() throws JingException {
        JSONObject skins = new JSONObject();

        JSONObject defaultSkin = readSkin(nonessential, 0, "default");
        if (null != defaultSkin && !defaultSkin.isEmpty()) {
            skins.element("default", defaultSkin);
        }
        int count = readInt(true);
        JSONObject skin;
        String skinName;
        for (int i$ = 0; i$ < count; i$++) {
            skinName = readString();
            skin = readSkin(nonessential, i$ + 1, skinName);
            if (null != skin && !skin.isEmpty()) {
                skins.element(skinName, skin);
            }
        }

        return skins;
    }

    private JSONObject createEvents() throws JingException {
        JSONObject events = new JSONObject();
        int count = readInt(true);
        String eventName;
        JSONObject event;
        for (int i$ = 0; i$ < count; i$++) {
            eventName = readString();
            event = new JSONObject();
            event.element("int", readInt(false));
            event.element("float", readFloat());
            event.element("string", readString());
            events.element(eventName, event);
            eventsNameList.add(eventName);
        }
        return events;
    }

    private JSONObject createAnimations() throws JingException {
        JSONObject animations = new JSONObject();
        int count = readInt(true);
        for (int i$ = 0; i$ < count; i$++) {
            readAnimation(readString(), animations);
        }
        return animations;
    }

    public String rgba8888ToString(int colorInt) {
        float r = (float)((colorInt & -16777216) >>> 24) / 255.0F;
        float g = (float)((colorInt & 16711680) >>> 16) / 255.0F;
        float b = (float)((colorInt & '\uff00') >>> 8) / 255.0F;
        float a = (float)(colorInt & 255) / 255.0F;
        StringBuilder value = new StringBuilder(Integer.toHexString(
            (int) (255.0F * r) << 24 | (int) (255.0F * g) << 16 | (int) (255.0F * b) << 8 | (int) (255.0F * a)));
        while (value.length() < 8) {
            value.insert(0, "0");
        }

        return value.toString();
    }

    private <T> T getList(List<T> list, int index) {
        if (null == list || list.size() <= index) {
            return null;
        }
        else {
            return list.get(index);
        }
    }

    private <T> void setList(List<T> list, int index, T value) {
        if (null == list || list.size() <= index) {
            if (null != list) {
                list.add(value);
            }
        }
        else {
            list.set(index, value);
        }
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public JSONObject getRetJson() {
        return retJson;
    }

    public static void main(String[] args) throws JingException {
        String skelFilePath = "E:\\W\\WorkSpace\\JS\\spine-runtimes-3.8\\spine-runtimes-3.8\\spine-ts\\player\\example\\assets\\char_166_skfire.skel";
        Arknights decrypter = new Arknights(skelFilePath);
        decrypter.decrypter();
    }
}
