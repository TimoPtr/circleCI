SOURCE_SET_SDK = %r{(SDK/.*)/src/.*}.freeze
SOURCE_SET_APP = %r{(MainApp/.*)/src/.*}.freeze
MODULE_NAME = %r{.*/(.*)}.freeze

# @return an Array of String containing all the modules which has been modified in this Pull Request
def get_modified_modules(modified_files)
  captured_modules = modified_files.map do |file|
    (file.to_s.match(SOURCE_SET_SDK) || file.to_s.match(SOURCE_SET_APP))&.captures
  end.compact.uniq

  # Get the captured expression from the regex, which is the Android module(e.g 'SDK/HomeUIHUM' or 'MainApp/app')
  captured_modules.map { |captured_module| captured_module[0] }
end

# @return the same list as `get_modified_modules` but instead it trims this list from `SDK` or `MainApp` prefixes
def get_modified_modules_names(modified_files)
  get_modified_modules(modified_files).map do |and_mod|
    and_mod.match(MODULE_NAME).captures[0]
  end
end
