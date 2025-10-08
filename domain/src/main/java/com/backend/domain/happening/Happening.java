package com.backend.domain.happening;

import com.backend.domain.actor.ActorId;
import com.backend.domain.location.LocationId;
import com.backend.domain.media.Media;
import com.backend.domain.mixins.Actored;
import com.backend.domain.mixins.HasMedia;
import com.backend.domain.mixins.Locatable;
import com.backend.domain.mixins.Reactable;
import com.backend.domain.reactions.SentimentEngagement;
import java.util.Collections;
import java.util.Set;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Abstract aggregate root representing a Happening in the system.
 *
 * A Happening has a unique identifier, a title, and a description.
 * It is also associated with an actor, a location, media, and sentiment engagement
 * through the implemented mixin interfaces.
 */
@Getter
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public abstract class Happening implements
    Actored, Locatable, HasMedia, Reactable {


  // dont set Id final as JPA wont see it
  protected Long id;

  private final ActorId actorId;

  private final LocationId locationId;

  private final SentimentEngagement sentimentEngagement;

  @Getter
  private Set<Media> media;

  @Setter
  private String title;

  @Setter
  private String description;

  protected Happening(
          @NonNull Long id,
      @NonNull ActorId actorId,
      @NonNull LocationId locationId,
      @NonNull Set<Media> media,
      String title,
      String description) {
    this.id = id;
    this.actorId = actorId;
    this.locationId = locationId;
    this.sentimentEngagement = new SentimentEngagement(0, 0);
    this.media = Collections.unmodifiableSet(media);
    this.title = title;
    this.description = description;
  }

  @Override
  public ActorId actorId() {
    return actorId;
  }

  @Override
  public LocationId locationId() {
    return locationId;
  }

  @Override
  public SentimentEngagement sentimentEngagement() {
    return sentimentEngagement;
  }

  @Override
  public Set<Media> media() {
    return media;
  }
}
