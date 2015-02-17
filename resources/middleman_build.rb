require 'bundler/setup'
require 'bundler'
require 'middleman-core/profiling'
Middleman::Profiling.start

# Change directory to the root
Dir.chdir(ENV["MM_ROOT"]) if ENV["MM_ROOT"]

# Prevent middleman from invoking bundler for us
module Middleman
  class << self
    def setup_load_paths
      @_is_setup ||= begin
        require 'middleman-core/extensions'
        ::Middleman.load_extensions_in_path
        true
      end
    end
  end
end

Middleman.setup_load_paths
Bundler.require

require "middleman-core/cli"
require "middleman-core/application"

# Silly cache always needs reset
Middleman::Cli::Build.instance_variable_set("@_shared_instance", nil)
Middleman::Cli::Build.shared_instance.set(:build_dir, ENV["MM_BUILD"])

# Do it!
Middleman::Cli::Build.new([]).invoke(:build)
