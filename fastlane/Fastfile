# This file contains the fastlane.tools configuration
# You can find the documentation at https://docs.fastlane.tools
#
# For a list of all available actions, check out
#
#     https://docs.fastlane.tools/actions
#
# For a list of all available plugins, check out
#
#     https://docs.fastlane.tools/plugins/available-plugins
#

fastlane_version "2.220.0"

default_platform(:android)

platform :android do

    desc "Deploy to Alpha"
    lane :deploy_to_alpha do
        upload_to_play_store(track: 'alpha', aab: 'app/build/outputs/bundle/release/app-release.aab')
    end
end
