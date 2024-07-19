/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.inlong.sdk.transform.decode;

import org.apache.inlong.sdk.transform.pojo.FieldInfo;
import org.apache.inlong.sdk.transform.pojo.KvSourceInfo;

import org.apache.commons.lang3.StringUtils;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

/**
 * KvSourceDecoder
 * 
 */
public class KvSourceDecoder implements SourceDecoder<String> {

    protected KvSourceInfo sourceInfo;
    private Charset srcCharset = Charset.defaultCharset();
    private List<FieldInfo> fields;

    public KvSourceDecoder(KvSourceInfo sourceInfo) {
        this.sourceInfo = sourceInfo;
        if (!StringUtils.isBlank(sourceInfo.getCharset())) {
            this.srcCharset = Charset.forName(sourceInfo.getCharset());
        }
        this.fields = sourceInfo.getFields();
    }

    @Override
    public SourceData decode(byte[] srcBytes, Map<String, Object> extParams) {
        String srcString = new String(srcBytes, srcCharset);
        return this.decode(srcString, extParams);
    }

    @Override
    public SourceData decode(String srcString, Map<String, Object> extParams) {
        List<Map<String, String>> rowValues = KvUtils.splitKv(srcString, '&', '=', '\\', '\"', '\n');
        KvSourceData sourceData = new KvSourceData();
        if (fields == null || fields.size() == 0) {
            for (Map<String, String> row : rowValues) {
                sourceData.addRow();
                row.forEach((k, v) -> sourceData.putField(k, v));
            }
            return sourceData;
        }
        for (Map<String, String> row : rowValues) {
            sourceData.addRow();
            for (FieldInfo field : fields) {
                String fieldName = field.getName();
                String fieldValue = row.get(fieldName);
                sourceData.putField(fieldName, fieldValue);
            }
        }
        return sourceData;
    }
}
