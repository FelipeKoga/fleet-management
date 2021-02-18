package co.tcc.koga.android.utils

import co.tcc.koga.android.data.database.entity.UserEntity

fun getUserAvatar(userEntity: UserEntity, size: Int = 200): String {
    return "${CONSTANTS.AVATAR_BASE_URL}&name=${userEntity.name}&background=${userEntity.color}&size=$size"
}