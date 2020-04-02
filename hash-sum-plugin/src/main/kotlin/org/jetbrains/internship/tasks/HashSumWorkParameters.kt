package org.jetbrains.internship.tasks

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.workers.WorkParameters

interface HashSumWorkParameters : WorkParameters {
    val inputDirectory: DirectoryProperty
    val outputFile: RegularFileProperty
    var alg: Property<String>
    var extensions: ListProperty<String>
}
