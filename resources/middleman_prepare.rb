require 'bundler'

Dir.chdir(ENV["MM_ROOT"]) if ENV["MM_ROOT"]

missing = false

begin
  missing = Bundler.definition.missing_specs.any?
rescue Bundler::GemNotFound, Bundler::VersionConflict
  missing = true
end

if missing
  puts "<< Installing missing middleman dependencies to #{ENV["GEM_HOME"]} >>"
  Bundler.clear_gemspec_cache
  Bundler.reset!
  Gem.load_env_plugins if Gem.respond_to?(:load_env_plugins)
  definition = Bundler.definition
  definition.validate_ruby!
  Bundler::Installer.install(Bundler.root, definition, {})
  Bundler.clear_gemspec_cache
  Bundler.reset!
end
