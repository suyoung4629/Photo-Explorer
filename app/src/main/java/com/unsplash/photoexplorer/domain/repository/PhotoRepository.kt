package com.unsplash.photoexplorer.domain.repository

import androidx.paging.PagingData
import com.unsplash.photoexplorer.domain.model.Photo
import com.unsplash.photoexplorer.domain.model.PhotoDetail
import kotlinx.coroutines.flow.Flow

interface PhotoRepository {

    /**
     * Unsplash 사진 목록을 페이지 단위로 스트림.
     *
     * 각 [Photo.isFavorite]는 로컬 DB의 즐겨찾기 상태와 자동 동기화되어,
     * 좋아요 토글 시 별도 새로고침 없이 갱신된다.
     */
    fun getPhotoList(): Flow<PagingData<Photo>>

    /**
     * 사진 단건의 상세 정보를 조회.
     *
     * `PhotoDetail.photo.isFavorite`는 호출 시점의 DB 상태를 반영.
     * 네트워크/파싱 실패 시 예외를 throw한다.
     */
    suspend fun getPhotoDetail(id: String): PhotoDetail

    /**
     * 좋아요(저장) 상태를 토글한다.
     *
     * - OFF → ON: trackDownload API → 이미지 다운로드 → 내부저장소 저장 → DB insert.
     * - ON → OFF: 로컬 파일 삭제 후 DB 삭제. 파일 삭제가 실패하면 DB 삭제도 진행하지 않는다.
     *
     */
    suspend fun toggleFavorite(photo: Photo)

    /**
     * 즐겨찾기로 저장된 사진들을 추가한 시각의 내림차순으로 스트림.
     *
     * 반환되는 [Photo.localPath]에는 내부저장소의 파일 절대경로가 채워져 있어,
     * UI는 이 경로로 오프라인 표시가 가능하다.
     */
    fun observeFavoritePhotos(): Flow<List<Photo>>

    /**
     * Unsplash 다운로드 트래킹 엔드포인트를 호출하고 풀 화질 이미지 URL을 반환.
     *
     * Unsplash 정책상 사용자가 사진을 다운로드/저장하기 전 반드시 호출해야 한다.
     * 반환된 URL은 호출마다 다르며 트래킹 파라미터를 포함한다.
     */
    suspend fun trackDownload(photoId: String): String
}
