/*
 * Copyright 2026 UBIK inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.ubik;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.annotations.SerializedName;
import io.swagger.v3.oas.models.media.Schema;
import io.ubik.cdr.AnsweredBy;
import io.ubik.cdr.CallerId;
import io.ubik.cdr.CdrDetail;
import io.ubik.cdr.CdrItem;
import io.ubik.cdr.Direction;
import io.ubik.cdr.Disposition;
import io.ubik.cdr.ExtensionSnapshot;
import io.ubik.cdr.ExtensionView;
import io.ubik.cdr.HangupBy;
import io.ubik.cdr.IvrSnapshot;
import io.ubik.cdr.IvrView;
import io.ubik.cdr.LinkItem;
import io.ubik.cdr.LinkKind;
import io.ubik.cdr.LinkRelation;
import io.ubik.cdr.PhoneNumberSnapshot;
import io.ubik.cdr.PhoneNumberView;
import io.ubik.cdr.PhoneSnapshot;
import io.ubik.cdr.PhoneView;
import io.ubik.cdr.QualityInfo;
import io.ubik.cdr.RecordingChannel;
import io.ubik.cdr.RecordingChannelPhone;
import io.ubik.cdr.RecordingChannelUser;
import io.ubik.cdr.RecordingDownload;
import io.ubik.cdr.RecordingItem;
import io.ubik.cdr.RecordingPhone;
import io.ubik.cdr.RecordingState;
import io.ubik.cdr.ResourceRef;
import io.ubik.cdr.ResourceType;
import io.ubik.cdr.SessionCallerId;
import io.ubik.cdr.Side;
import io.ubik.cdr.SnapshotAddress;
import io.ubik.cdr.SnapshotBusinessHours;
import io.ubik.cdr.SnapshotCallerId;
import io.ubik.cdr.SnapshotExtensionCalls;
import io.ubik.cdr.SnapshotPhoneNumberRoute;
import io.ubik.cdr.SnapshotTimeRange;
import io.ubik.cdr.TimelineEntry;
import io.ubik.cdr.TimelineResource;
import io.ubik.cdr.UserView;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

/**
 * Each model class holds exactly the fields of its schema in the public CDR
 * OpenAPI file, and each enum exactly its values. A field the API adds and
 * the library misses, or a renamed one, fails here instead of reaching an
 * integrator as a silent null.
 */
class CdrModelContractTest {

  private static final Map<String, Class<?>> MODELS = new LinkedHashMap<>();

  static {
    MODELS.put("CDRItem", CdrItem.class);
    MODELS.put("CDRDetail", CdrDetail.class);
    MODELS.put("TimelineEntry", TimelineEntry.class);
    MODELS.put("TimelineResource", TimelineResource.class);
    MODELS.put("LinkItem", LinkItem.class);
    MODELS.put("ResourceRef", ResourceRef.class);
    MODELS.put("ExtensionView", ExtensionView.class);
    MODELS.put("PhoneView", PhoneView.class);
    MODELS.put("UserView", UserView.class);
    MODELS.put("IVRView", IvrView.class);
    MODELS.put("PhoneNumberView", PhoneNumberView.class);
    MODELS.put("QualityInfo", QualityInfo.class);
    MODELS.put("RecordingItem", RecordingItem.class);
    MODELS.put("RecordingPhone", RecordingPhone.class);
    MODELS.put("RecordingChannel", RecordingChannel.class);
    MODELS.put("SessionCallerID", SessionCallerId.class);
    MODELS.put("CallerID", CallerId.class);
    MODELS.put("RecordingChannelPhone", RecordingChannelPhone.class);
    MODELS.put("RecordingChannelUser", RecordingChannelUser.class);
    MODELS.put("RecordingDownload", RecordingDownload.class);
    MODELS.put("PhoneSnapshot", PhoneSnapshot.class);
    MODELS.put("PhoneNumberSnapshot", PhoneNumberSnapshot.class);
    MODELS.put("ExtensionSnapshot", ExtensionSnapshot.class);
    MODELS.put("IVRSnapshot", IvrSnapshot.class);
    MODELS.put("SnapshotAddress", SnapshotAddress.class);
    MODELS.put("SnapshotCallerID", SnapshotCallerId.class);
    MODELS.put("SnapshotTimeRange", SnapshotTimeRange.class);
    MODELS.put("SnapshotBusinessHours", SnapshotBusinessHours.class);
    MODELS.put("SnapshotPhoneNumberRoute", SnapshotPhoneNumberRoute.class);
    MODELS.put("SnapshotExtensionCalls", SnapshotExtensionCalls.class);
  }

  @Test
  void eachModelHasExactlyItsSchemaFields() {
    Map<String, Schema> schemas = PublicOpenAPI.CDR.model().getComponents().getSchemas();
    for (Map.Entry<String, Class<?>> model : MODELS.entrySet()) {
      Schema<?> schema = schemas.get(model.getKey());
      TreeSet<String> wire = new TreeSet<>(schema.getProperties().keySet());
      TreeSet<String> java = new TreeSet<>();
      for (Field field : model.getValue().getDeclaredFields()) {
        SerializedName name = field.getAnnotation(SerializedName.class);
        if (name != null) {
          java.add(name.value());
        }
      }
      assertEquals(wire, java, model.getKey() + " vs " + model.getValue().getSimpleName());
    }
  }

  @Test
  void eachEnumHasExactlyItsSchemaValues() {
    Map<String, Schema> schemas = PublicOpenAPI.CDR.model().getComponents().getSchemas();
    assertEnum(Direction.class, enumOf(schemas, "CDRItem", "direction"));
    assertEnum(Direction.class, enumOf(schemas, "CDRDetail", "direction"));
    assertEnum(AnsweredBy.class, enumOf(schemas, "CDRItem", "answered_by"));
    assertEnum(HangupBy.class, enumOf(schemas, "CDRDetail", "hangup_by"));
    assertEnum(LinkKind.class, enumOf(schemas, "LinkItem", "kind"));
    assertEnum(LinkRelation.class, enumOf(schemas, "LinkItem", "relation"));
    assertEnum(ResourceType.class, enumOf(schemas, "ResourceRef", "resource_type"));
    assertEnum(RecordingState.class, enumOf(schemas, "RecordingItem", "state"));
    assertEnum(Side.class, enumOf(schemas, "RecordingChannel", "side"));

    // Disposition lists its values in its description: "- VALUE: meaning."
    List<String> dispositions = new ArrayList<>();
    Matcher m =
        Pattern.compile("(?m)^- ([A-Z_]+):").matcher(schemas.get("Disposition").getDescription());
    while (m.find()) {
      dispositions.add(m.group(1));
    }
    assertEquals(13, dispositions.size());
    assertEnum(Disposition.class, dispositions);
  }

  private static List<String> enumOf(Map<String, Schema> schemas, String schema, String property) {
    List<String> values = new ArrayList<>();
    for (Object value : ((Schema<?>) schemas.get(schema).getProperties().get(property)).getEnum()) {
      values.add(String.valueOf(value));
    }
    return values;
  }

  private static <E extends Enum<E>> void assertEnum(Class<E> type, List<String> wire) {
    List<String> java = new ArrayList<>();
    for (E constant : type.getEnumConstants()) {
      if (!constant.name().equals("UNRECOGNIZED")) {
        try {
          java.add((String) type.getMethod("getValue").invoke(constant));
        } catch (ReflectiveOperationException e) {
          throw new AssertionError(e);
        }
      }
    }
    assertEquals(new TreeSet<>(wire), new TreeSet<>(java), type.getSimpleName());
  }
}
