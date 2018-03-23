package spade.lang.static

trait Statics

/** Internal view of Spade */
trait InternalStatics extends Statics with InternalAliases

/** External view for extending DSLs */
trait ExtensionStatics extends InternalStatics with ExternalAliases

/** Application view */
trait ExternalStatics extends ExtensionStatics {
  type SpadeDesign = spade.SpadeDesign
}