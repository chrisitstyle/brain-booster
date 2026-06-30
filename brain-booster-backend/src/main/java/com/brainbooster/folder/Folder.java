package com.brainbooster.folder;

import com.brainbooster.flashcardset.FlashcardSet;
import com.brainbooster.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Formula;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "folder")
public class Folder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "folder_id")
    private Long folderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 100)
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Builder.Default
    @ManyToMany
    @JoinTable(
            name = "folder_flashcard_set",
            joinColumns = @JoinColumn(name = "folder_id"),
            inverseJoinColumns = @JoinColumn(name = "set_id")
    )
    private Set<FlashcardSet> flashcardSets = new HashSet<>();

    @Formula("(select count(*) from folder_flashcard_set ffs where ffs.folder_id = folder_id)")
    private Long setCount;
}
