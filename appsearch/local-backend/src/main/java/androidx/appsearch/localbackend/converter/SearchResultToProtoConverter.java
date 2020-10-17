/*
 * Copyright 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.appsearch.localbackend.converter;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;
import androidx.appsearch.app.GenericDocument;
import androidx.appsearch.app.SearchResult;

import com.google.android.icing.proto.SearchResultProto;
import com.google.android.icing.proto.SnippetMatchProto;
import com.google.android.icing.proto.SnippetProto;

import java.util.ArrayList;

/**
 * Translates a {@link SearchResultProto} into {@link SearchResult}s.
 *
 * @hide
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public class SearchResultToProtoConverter {
    private SearchResultToProtoConverter() {}

    /** Translate a {@link SearchResultProto.ResultProto} into {@link SearchResult}. */
    @NonNull
    public static SearchResult convertSearchResult(
            @NonNull SearchResultProto.ResultProtoOrBuilder proto) {
        Bundle bundle = new Bundle();
        GenericDocument document = GenericDocumentToProtoConverter.convert(proto.getDocument());
        bundle.putBundle(SearchResult.DOCUMENT_FIELD, document.getBundle());

        ArrayList<Bundle> matchList = new ArrayList<>();
        if (proto.hasSnippet()) {
            for (int i = 0; i < proto.getSnippet().getEntriesCount(); i++) {
                SnippetProto.EntryProto entry = proto.getSnippet().getEntries(i);
                for (int j = 0; j < entry.getSnippetMatchesCount(); j++) {
                    Bundle matchInfoBundle = convertToMatchInfoBundle(
                            entry.getSnippetMatches(j), entry.getPropertyName());
                    matchList.add(matchInfoBundle);
                }
            }
        }
        bundle.putParcelableArrayList(SearchResult.MATCHES_FIELD, matchList);

        return new SearchResult(bundle);
    }

    private static Bundle convertToMatchInfoBundle(
            SnippetMatchProto snippetMatchProto, String propertyPath) {
        Bundle bundle = new Bundle();
        bundle.putString(SearchResult.MatchInfo.PROPERTY_PATH_FIELD, propertyPath);
        bundle.putInt(
                SearchResult.MatchInfo.VALUES_INDEX_FIELD, snippetMatchProto.getValuesIndex());
        bundle.putInt(
                SearchResult.MatchInfo.EXACT_MATCH_POSITION_LOWER_FIELD,
                snippetMatchProto.getExactMatchPosition());
        bundle.putInt(
                SearchResult.MatchInfo.EXACT_MATCH_POSITION_UPPER_FIELD,
                snippetMatchProto.getExactMatchPosition() + snippetMatchProto.getExactMatchBytes());
        bundle.putInt(
                SearchResult.MatchInfo.WINDOW_POSITION_LOWER_FIELD,
                snippetMatchProto.getWindowPosition());
        bundle.putInt(
                SearchResult.MatchInfo.WINDOW_POSITION_UPPER_FIELD,
                snippetMatchProto.getWindowPosition() + snippetMatchProto.getWindowBytes());
        return bundle;
    }
}
