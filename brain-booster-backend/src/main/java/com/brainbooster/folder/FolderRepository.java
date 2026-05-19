package com.brainbooster.folder;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FolderRepository extends JpaRepository<Folder, Long> {

    @Query("""
            SELECT DISTINCT f FROM Folder f
            JOIN FETCH f.user
            LEFT JOIN FETCH f.flashcardSets fs
            """)
    List<Folder> findAllWithSetsAndUser();

    @Query("""
            SELECT DISTINCT f FROM Folder f
            JOIN FETCH f.user
            LEFT JOIN FETCH f.flashcardSets fs
            WHERE f.user.userId = :userId
            """)
    List<Folder> findAllByUserId(Long userId);

    @Query("""
            SELECT DISTINCT f FROM Folder f
            JOIN FETCH f.user u
            LEFT JOIN FETCH f.flashcardSets fs
            WHERE u.nickname = :nickname
            """)
    List<Folder> findAllByUserNickname(String nickname);


    @Query("""
            SELECT DISTINCT f FROM Folder f
            JOIN FETCH f.user
            LEFT JOIN FETCH f.flashcardSets fs
            WHERE f.folderId = :folderId
            """)
    Optional<Folder> findByIdWithSetsAndUser(Long folderId);

    @Query("""
            SELECT f FROM Folder f
            JOIN FETCH f.user
            WHERE f.folderId = :folderId
            """)
    Optional<Folder> findByIdWithUser(Long folderId);
}
