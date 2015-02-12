require 'middleman-core/profiling'
Middleman::Profiling.start

require "middleman-core/load_paths"
Middleman.setup_load_paths

require "middleman-core/cli"
require "middleman-core/application"

# Silly cache always needs reset
Middleman::Cli::Build.instance_variable_set("@_shared_instance", nil)

# Change directory to the root
Dir.chdir(ENV["MM_ROOT"]) if ENV["MM_ROOT"]

Middleman::Cli::Build.shared_instance.set(:build_dir, ENV["MM_BUILD"])

# Do it!
Middleman::Cli::Build.new([]).invoke(:build)
