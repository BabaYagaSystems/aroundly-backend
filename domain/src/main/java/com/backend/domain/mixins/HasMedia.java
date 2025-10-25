package com.backend.domain.mixins;

import com.backend.domain.media.Media;
import java.util.Set;

/**
 * Mixin interface for objects that provide access to a media resource.
 */
public interface HasMedia {

    /**
     * Returns the media resource associated with the implementing object.
     *
     * @return the media resource
     */
    Set<Media> getMedia();

    default boolean addMedia(Media media) {
        return getMedia().add(media);
    }

    default boolean removeMedia(Media media) {
        return getMedia().remove(media);
    }
}
