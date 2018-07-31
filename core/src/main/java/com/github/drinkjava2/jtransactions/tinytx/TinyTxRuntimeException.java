/*
 * Copyright (C) 2016 Yong Zhu.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.github.drinkjava2.jtransactions.tinytx;

/**
 * This TinyTxRuntimeException used to wrap exception to a Runtime type
 * Exception
 * 
 * @author Yong Zhu
 * @since 1.0.0
 */
public class TinyTxRuntimeException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public TinyTxRuntimeException() {
		super();
	}

	public TinyTxRuntimeException(Throwable cause) {
		super(cause);
	}

	public TinyTxRuntimeException(String msg) {
		super(msg);
	}

	public TinyTxRuntimeException(String msg, Throwable cause) {
		super(msg, cause);
	}

	public static void assertNotNull(Object object, String msg) {
		if (object == null)
			throw new TinyTxRuntimeException(msg);
	}
}
