package org.jboss.windup.rules.apps.java.archives.config;

import java.io.File;
import java.util.logging.Logger;

import org.jboss.forge.furnace.util.Visitor;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.operation.GraphOperation;
import org.jboss.windup.config.phase.Initialization;
import org.jboss.windup.config.phase.RulePhase;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.rules.apps.java.archives.identify.IdentifiedArchives;
import org.jboss.windup.util.Logging;
import org.jboss.windup.util.WindupPathUtil;
import org.jboss.windup.util.exception.WindupException;
import org.jboss.windup.util.file.FileSuffixPredicate;
import org.jboss.windup.util.file.FileVisit;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * Loads configuration/metadata for {@link ArchiveIdentificationRuleProvider}.
 * 
 * @author <a href="mailto:ozizka@redhat.com">Ondrej Zizka</a>
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class ArchiveIdentificationConfigLoadingRuleProvider extends WindupRuleProvider
{
    private static final Logger log = Logging.get(ArchiveIdentificationConfigLoadingRuleProvider.class);

    @Override
    public Class<? extends RulePhase> getPhase()
    {
        return Initialization.class;
    }

    @Override
    public Configuration getConfiguration(final GraphContext grCtx)
    {
        ConfigurationBuilder config = ConfigurationBuilder.begin();
        config.addRule().perform(new GraphOperation()
        {
            public void perform(GraphRewrite event, EvaluationContext evCtx)
            {
                Visitor<File> visitor = new Visitor<File>()
                {
                    @Override
                    public void visit(File file)
                    {
                        try
                        {
                            log.info("Loading archive identification metadata from [" + file.getAbsolutePath() + "]");
                            IdentifiedArchives.addMappingsFrom(file);
                        }
                        catch (Exception e)
                        {
                            throw new WindupException("Failed to load metadata from file [" + file + "]", e);
                        }
                    }
                };

                FileSuffixPredicate predicate = new FileSuffixPredicate("\\.archive-metadata\\.txt");
                FileVisit.visit(WindupPathUtil.getUserCacheDir().toFile(), predicate, visitor);
                FileVisit.visit(WindupPathUtil.getWindupCacheDir().toFile(), predicate, visitor);
            }
        });
        return config;
    }
}
