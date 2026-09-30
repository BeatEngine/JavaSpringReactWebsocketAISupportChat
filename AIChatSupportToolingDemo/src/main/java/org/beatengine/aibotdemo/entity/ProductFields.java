package org.beatengine.aibotdemo.entity;

/** Serialize all ProductFields classes as ProductRecord for AI <p></p>
 * because of circle-serialization protection at db-relations*/
//@JsonSerialize(as = ProductFields.class)   // Output (Tool -> LLM)
//@JsonDeserialize(as = ProductFields.class)       // INPUT (LLM -> Tool)

public interface ProductFields {

}
