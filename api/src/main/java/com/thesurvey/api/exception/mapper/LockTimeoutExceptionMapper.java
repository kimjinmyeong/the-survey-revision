package com.thesurvey.api.exception.mapper;

import com.thesurvey.api.exception.ErrorMessage;

public class LockTimeoutExceptionMapper extends RuntimeException {

    public LockTimeoutExceptionMapper(ErrorMessage errorMessage) {
        super(errorMessage.getMessage());
    }

}
