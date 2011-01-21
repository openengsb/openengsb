#
# Copyright 2010 OpenEngSB Division, Vienna University of Technology
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#


# prints dependencies from all pom files recursively. With sort and uniq unix tools
# can be easily listed
require "rexml/document"

def print_dependencies deps
  deps.elements.each("*/dependencies/dependency"){|element| 
    puts element.to_s.gsub("\n", "").gsub(" ", "").gsub("\t", "")
    # Uncomment next line to have all version tags removed from the pom files, so they inherit from parent pom
    #    element.delete_element("version")
  }
end

def iterate_over_dir dir
  dir.each{|name|
    dirname = "#{dir.path}/#{name}"
    if name == "pom.xml"
      begin
        doc = REXML::Document.new File.new(dirname)
        puts dirname
        print_dependencies doc
        File.open(dirname, "w"){|file| 
          result = String.new
          doc.write result
          result = result.to_s.gsub("\'", "\"").gsub("      \n", "")
          file.write(result)
        }
      rescue REXML::ParseException
      end
    elsif File.directory?(dirname) && name != "." && name != ".."
      Dir.open(dirname){|newdir| iterate_over_dir newdir}
    end
  }
end
# Enter directory here
dir = Dir.open("") 
iterate_over_dir dir
dir.close

