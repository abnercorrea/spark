/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.spark.sql.execution.command.v2

import org.apache.spark.sql.AnalysisException
import org.apache.spark.sql.execution.command

class AlterTableDropPartitionSuite
  extends command.AlterTableDropPartitionSuiteBase
  with CommandSuiteBase {

  override protected val notFullPartitionSpecErr = "Partition spec is invalid"

  test("SPARK-33650: drop partition into a table which doesn't support partition management") {
    withNamespaceAndTable("ns", "tbl", s"non_part_$catalog") { t =>
      sql(s"CREATE TABLE $t (id bigint, data string) $defaultUsing")
      val errMsg = intercept[AnalysisException] {
        sql(s"ALTER TABLE $t DROP PARTITION (id=1)")
      }.getMessage
      assert(errMsg.contains("can not alter partitions"))
    }
  }

  test("purge partition data") {
    withNamespaceAndTable("ns", "tbl") { t =>
      sql(s"CREATE TABLE $t (id bigint, data string) $defaultUsing PARTITIONED BY (id)")
      sql(s"ALTER TABLE $t ADD PARTITION (id=1)")
      try {
        val errMsg = intercept[UnsupportedOperationException] {
          sql(s"ALTER TABLE $t DROP PARTITION (id=1) PURGE")
        }.getMessage
        assert(errMsg.contains("purge is not supported"))
      } finally {
        sql(s"ALTER TABLE $t DROP PARTITION (id=1)")
      }
    }
  }
}
