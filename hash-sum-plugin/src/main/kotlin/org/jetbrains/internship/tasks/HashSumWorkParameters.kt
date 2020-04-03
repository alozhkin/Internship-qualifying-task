package org.jetbrains.internship.tasks

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.workers.WorkParameters

interface HashSumWorkParameters : WorkParameters {
    val alg: Property<String>
    val fileExt: ListProperty<String>
    val inputDirectory: DirectoryProperty
    val outputFile: RegularFileProperty
}
