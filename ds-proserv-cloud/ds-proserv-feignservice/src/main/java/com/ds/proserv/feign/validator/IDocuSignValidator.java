package com.ds.proserv.feign.validator;

import com.ds.proserv.feign.domain.IDocuSignInformation;

public interface IDocuSignValidator<T extends IDocuSignInformation> {

	void validateSaveData(T docusignInformation);
}