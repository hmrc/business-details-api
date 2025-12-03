/*
 * Copyright 2025 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package utils

import play.api.libs.json.*

object JsonTransformers {

  def conditionalUpdate(path: JsPath, transform: JsValue => JsValue): Reads[JsObject] =
    Reads[JsObject] { json =>
      path.readNullable[JsValue].reads(json).flatMap {
        case Some(value) => JsPath.json.update(path.json.put(transform(value))).reads(json)
        case None        => JsSuccess(json.as[JsObject])
      }
    }

  def conditionalCopy(sourcePath: JsPath, targetPath: JsPath): Reads[JsObject] =
    Reads[JsObject] { json =>
      sourcePath.readNullable[JsValue].reads(json).flatMap {
        case Some(value) => JsPath.json.update(targetPath.json.put(value)).reads(json)
        case None        => JsSuccess(json.as[JsObject])
      }
    }

}
