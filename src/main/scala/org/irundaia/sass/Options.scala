/*
 * Copyright 2017 Han van Venrooij
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

package org.irundaia.sass

import java.io.File
import java.nio.file.{Path, Paths}

import org.irundaia.sass.jna.SassLibrary

import scala.language.implicitConversions

case class Options(nativeOptions: SassLibrary.Sass_Options) {
  def precision: Int = SassLibrary.INSTANCE.sass_option_get_precision(this.nativeOptions)
  def precision_=(precision: Int): Unit = SassLibrary.INSTANCE.sass_option_set_precision(this.nativeOptions, precision)

  def outputStyle: CssStyle = {
    SassLibrary.INSTANCE.sass_option_get_output_style(this.nativeOptions) match {
      case SassLibrary.Sass_Output_Style.SASS_STYLE_NESTED => Sassy
      case SassLibrary.Sass_Output_Style.SASS_STYLE_EXPANDED => Maxified
      case SassLibrary.Sass_Output_Style.SASS_STYLE_COMPRESSED => Minified
      case style =>
        throw new IllegalStateException(s"Unknown Sass output style: $style")
    }
  }
  def outputStyle_=(outputStyle: CssStyle): Unit = SassLibrary.INSTANCE.sass_option_set_output_style(this.nativeOptions, outputStyle.intValue)

  def sourceComments: Boolean = SassLibrary.INSTANCE.sass_option_get_source_comments(this.nativeOptions)
  def sourceComments_=(b: Boolean): Unit =
    SassLibrary.INSTANCE.sass_option_set_source_comments(this.nativeOptions, b)

  def sourceMapEmbed: Boolean = SassLibrary.INSTANCE.sass_option_get_source_map_embed(this.nativeOptions)
  def sourceMapEmbed_=(sourceMapEmbed: Boolean): Unit =
    SassLibrary.INSTANCE.sass_option_set_source_map_embed(this.nativeOptions, sourceMapEmbed)

  def sourceMapContents: Boolean = SassLibrary.INSTANCE.sass_option_get_source_map_embed(this.nativeOptions)
  def sourceMapContents_=(b: Boolean): Unit = SassLibrary.INSTANCE.sass_option_set_source_map_contents(this.nativeOptions, b)

  def omitSourceMapUrl: Boolean = SassLibrary.INSTANCE.sass_option_get_omit_source_map_url(this.nativeOptions)
  def omitSourceMapUrl_=(b: Boolean): Unit = SassLibrary.INSTANCE.sass_option_set_omit_source_map_url(this.nativeOptions, b)

  def indentedSyntaxSrc: Boolean = SassLibrary.INSTANCE.sass_option_get_is_indented_syntax_src(this.nativeOptions)
  def indentedSyntaxSrc_=(b: Boolean): Unit = SassLibrary.INSTANCE.sass_option_set_is_indented_syntax_src(this.nativeOptions, b)

  def inputPath: Path = Paths.get(SassLibrary.INSTANCE.sass_option_get_input_path(this.nativeOptions))
  def inputPath_=(p: Path): Unit = inputPath_=(p.toFile.getAbsolutePath)
  def inputPath_=(p: String): Unit = SassLibrary.INSTANCE.sass_option_set_input_path(this.nativeOptions, p)

  def outputPath: Path = Paths.get(SassLibrary.INSTANCE.sass_option_get_output_path(this.nativeOptions))
  def outputPath_=(p: Path): Unit = outputPath_=(p.toFile.getAbsolutePath)
  def outputPath_=(p: String): Unit = SassLibrary.INSTANCE.sass_option_set_output_path(this.nativeOptions, p)

  def sourceMapPath: String = SassLibrary.INSTANCE.sass_option_get_source_map_file(this.nativeOptions)
  def sourceMapPath_=(file: Path): Unit = sourceMapPath_=(file.toFile.getAbsolutePath)
  def sourceMapPath_=(file: String): Unit = SassLibrary.INSTANCE.sass_option_set_source_map_file(this.nativeOptions, file)

  def includePaths: Iterable[Path] = {
    val includePathSize = SassLibrary.INSTANCE.sass_option_get_include_path_size(this.nativeOptions)

    if (includePathSize.longValue() == 0) {
      Seq()
    } else {
      Option(SassLibrary.INSTANCE.sass_option_get_include_path(this.nativeOptions, includePathSize)) match {
        case None => Seq()
        case Some(includePathsString) => includePathsString.split(File.pathSeparator).map(Paths.get(_))
      }
    }
  }
  def includePaths_=(paths: String): Unit = SassLibrary.INSTANCE.sass_option_set_include_path(this.nativeOptions, paths)
  def includePaths_=(paths: Path*): Unit = includePaths_=(paths.map(_.toFile.getAbsolutePath).mkString(File.pathSeparator))
  def includePaths_=(paths: Iterable[Path]): Unit = includePaths_=(paths.map(_.toFile.getAbsolutePath).mkString(File.pathSeparator))
  def includePaths_+=(path: String): Unit = includePaths_+=(Paths.get(path))
  def includePaths_+=(path: Path): Unit = includePaths_++=(path)
  def includePaths_++=(paths: Path*): Unit = paths.map(_.toString).foreach(SassLibrary.INSTANCE.sass_option_push_include_path(nativeOptions, _))

  def sourceMapRoot: String = SassLibrary.INSTANCE.sass_option_get_source_map_root(this.nativeOptions)
  def sourceMapRoot_=(path: Path): Unit = sourceMapRoot_=(path.toFile.getAbsolutePath)
  def sourceMapRoot_=(path: String): Unit = SassLibrary.INSTANCE.sass_option_set_source_map_root(this.nativeOptions, path)

  def indent_=(indent: Int):Unit = SassLibrary.INSTANCE.sass_option_set_indent(nativeOptions, " " * indent)
}

object Options {
  def apply(context: Context): Options = new Options(SassLibrary.INSTANCE.sass_file_context_get_options(context.nativeContext))
}
