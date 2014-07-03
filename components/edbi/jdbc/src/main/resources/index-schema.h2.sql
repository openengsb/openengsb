--
-- Licensed to the Austrian Association for Software Tool Integration (AASTI)
-- under one or more contributor license agreements. See the NOTICE file
-- distributed with this work for additional information regarding copyright
-- ownership. The AASTI licenses this file to you under the Apache License,
-- Version 2.0 (the "License"); you may not use this file except in compliance
-- with the License. You may obtain a copy of the License at
--
--     http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.
--

CREATE TABLE IF NOT EXISTS `INDEX_INFORMATION` (
  `NAME`          VARCHAR(500) PRIMARY KEY,
  `CLASS`         VARCHAR(500),
  `TABLE_HEAD`    VARCHAR(100),
  `TABLE_HISTORY` VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS `INDEX_FIELD_INFORMATION` (
  `INDEX_NAME`  VARCHAR(500),
  `NAME`        VARCHAR(500),
  `TYPE`        VARCHAR(500),
  `MAPPED_NAME` VARCHAR(100),
  `MAPPED_TYPE` INT,
  `MAPPED_TYPE_NAME`  VARCHAR(100),
  `MAPPED_TYPE_SCALE` INT,

  FOREIGN KEY (`INDEX_NAME`) REFERENCES `INDEX_INFORMATION` (`NAME`) ON DELETE CASCADE
);
