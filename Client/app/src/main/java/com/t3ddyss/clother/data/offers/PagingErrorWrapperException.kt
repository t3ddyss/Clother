package com.t3ddyss.clother.data.offers

import com.t3ddyss.core.domain.models.ApiCallError

class PagingErrorWrapperException(val source: ApiCallError) : RuntimeException()